package com.cburch.logisim.in;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.List;

import com.cburch.draw.util.EditableLabel;
import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.tools.key.KeyConfigurator;
import com.cburch.logisim.util.EventSourceWeakSupport;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.UnmodifiableList;

public abstract class ManagedInstance<S extends State>
		implements Instance<S>, InstanceInteractive, Cloneable {
	public static final int APPEARANCE = 1;
	public static final int STATE = 2;
	public static final int BOUNDS = 4;
	public static final int PORTS = 8;
	public static final int ATTRIBUTES = 16;
	public static final int ATTRIBUTE_LIST = 32;
	public static final int LABEL = 64;
	
	private SharedData shared;
	private EventSourceWeakSupport<InstanceListener> listeners;
	private Location location;
	private Bounds bds;
	private List<Port> ports;
	private EditableLabel label;
	
	protected ManagedInstance(String saveName, StringGetter displayName) {
		shared = new SharedData(saveName, displayName);
		listeners = new EventSourceWeakSupport<InstanceListener>();
		location = Location.create(0, 0);
		bds = Bounds.EMPTY_BOUNDS;
		ports = Collections.emptyList();
		label = null;
	}

	public final void addInstanceListenerWeak(InstanceListener listener) {
		listeners.add(listener);
	}

	//
	// methods required by the Instance interface
	//
	@Override
	public ManagedInstance<S> clone() {
		try {
			@SuppressWarnings("unchecked")
			ManagedInstance<S> ret = (ManagedInstance<S>) super.clone();
			ret.listeners = new EventSourceWeakSupport<InstanceListener>();
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new UnsupportedOperationException("unexpected instance clone error");
		}
	}

	public Instance<S> createDefault(LogisimVersion version, Location loc) {
		ManagedInstance<S> ret = this.clone();
		ret.setLocation(loc);
		return ret;
	}

	
	public final String getSaveName() {
		return shared.getSaveName();
	}
	
	public StringGetter getDisplayName() {
		return shared.getDisplayName();
	}

	public final Location getLocation() {
		return location;
	}
	
	public final Bounds getBounds() {
		return bds;
	}
	
	public final List<Port> getPorts() {
		return ports;
	}
	
	public List<Attribute<?>> getAttributes() {
		return shared.getAttributes();
	}
	
	public final void setLocation(Location value) {
		location = value;
		invalidate(BOUNDS | PORTS);
	}
	
	public final Object getValue(Attribute<?> attr) {
		Object ret = retrieveValue(attr);
		if (ret == null && label != null) {
			ret = shared.retrieveLabelValue(label, attr);
		}
		return ret;
	}
	
	public final void setValue(Attribute<?> attr, Object value) {
		Object oldValue = getValue(attr);
		boolean same = oldValue == null ? value == null : oldValue.equals(value);
		if (!same) {
			updateValue(attr, value);
			int mask = shared.getAffected(attr);
			InstanceEvent event = new InstanceEvent(this, mask | ATTRIBUTES,
					attr, oldValue, bds, ports);
			invalidate(mask, event);
		}
	}
	
	//
	// methods required by the InstanceInteractive interface
	//
	public EditableLabel getLabel() {
		return label;
	}
	
	public KeyConfigurator getKeyConfigurator() {
		return shared.getKeyConfigurator();
	}
	
	//
	// methods that exist for the subclass to invoke
	//
	protected final void shareAttributes(Attribute<?>[] attrs) {
		shared.setAttributes(attrs);
	}
	
	protected final void shareAttributeAffects(Attribute<?> attr, int changeMask) {
		shared.setAffected(attr, changeMask);
	}
	
	protected final void shareIconName(String value) {
		shared.setIconName(value);
	}
	
	protected final void shareKeyConfigurator(KeyConfigurator... configurators) {
		if (configurators == null || configurators.length == 0) {
			shared.setKeyConfigurator(null);
		} else if (configurators.length == 1) {
			shared.setKeyConfigurator(configurators[0]);
		} else {
			shared.setKeyConfigurator(JoinedConfigurator.create(configurators));
		}
	}
	
	protected final void shareLabelAttributes(Attribute<String> labelAttr) {
		shared.setLabelAttributes(labelAttr, null, null);
	}
	
	protected final void shareLabelAttributes(Attribute<String> labelAttr,
			Attribute<Font> fontAttr) {
		shared.setLabelAttributes(labelAttr, fontAttr, null);
	}
	
	protected final void shareLabelAttributes(Attribute<String> labelAttr,
			Attribute<Font> fontAttr, Attribute<Color> colorAttr) {
		shared.setLabelAttributes(labelAttr, fontAttr, colorAttr);
	}
	
	protected final Port newInput(int dx, int dy, BitWidth width) {
		return new Port(this, location.translate(dx, dy), Port.INPUT, width);
	}
	
	protected final Port newOutput(int dx, int dy, BitWidth width) {
		return new Port(this, location.translate(dx, dy), Port.OUTPUT, width);
	}
	
	protected final Port newPassive(int dx, int dy, BitWidth width) {
		return new Port(this, location.translate(dx, dy), Port.PASSIVE, width);
	}
	
	protected final Port newInOut(int dx, int dy, BitWidth width) {
		return new Port(this, location.translate(dx, dy), Port.INOUT, width);
	}
	
	protected final void invalidate(int changeMask) {
		InstanceEvent event = new InstanceEvent(this, changeMask, null, null,
				bds, ports);
		invalidate(changeMask, event);
	}
	
	private void invalidate(int changeMask, InstanceEvent event) {
		Location loc = location;
		if ((changeMask & BOUNDS) != 0) {
			bds = computeBounds().translate(loc.getX(), loc.getY());
		}
		if ((changeMask & PORTS) != 0) {
			ports = new UnmodifiableList<Port>(computePorts());
		}
		if ((changeMask & LABEL) != 0) {
			computeLabel();
		}
		
		if (!listeners.isEmpty()) {
			for (InstanceListener listener : listeners) {
				listener.instanceChanged(event);
			}
		}
	}
	
	protected void setLabelLocation(int x, int y, int halign, int valign) {
		if (label == null) {
			label = shared.createLabel();
		}
		label.setLocation(x, y);
		label.setHorizontalAlignment(halign);
		label.setVerticalAlignment(valign);
	}
	
	//
	// methods that exist for the subclass to override
	//
	protected void computeLabel() { }

	//
	// methods that the subclass must define
	//
	protected abstract Object retrieveValue(Attribute<?> attr);
	protected abstract void updateValue(Attribute<?> attr, Object value);
	protected abstract Bounds computeBounds();
	protected abstract Port[] computePorts();
	public abstract S createState();
	public abstract void propagate(S state);
	public abstract void paintInstance(Painter painter, S state);
}
