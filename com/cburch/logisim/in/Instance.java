package com.cburch.logisim.in;

import java.util.List;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.StringGetter;

public interface Instance<S extends State> extends Cloneable {
	public void addInstanceListenerWeak(InstanceListener listener);
	
	public String getSaveName();
	public StringGetter getDisplayName();
	public Location getLocation();
	public Bounds getBounds();
	public List<Port> getPorts();
	
	public Instance<S> clone();
	public Instance<S> createDefault(LogisimVersion version, Location loc);
	public List<Attribute<?>> getAttributes();
	public Object getValue(Attribute<?> attr);
	public void setValue(Attribute<?> attr, Object value);
	public void setLocation(Location value);
	
	public S createState();
	public void propagate(S state);
	public void paintInstance(Painter painter, S state);
}
