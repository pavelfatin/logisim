/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit.appear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentEvent;
import com.cburch.logisim.comp.ComponentListener;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.EventSourceWeakSupport;

public class CircuitPins {
	private class MyComponentListener
			implements ComponentListener, AttributeListener {
		public void endChanged(ComponentEvent e) {
			fireChanged();
		}
		public void componentInvalidated(ComponentEvent e) { }

		public void attributeListChanged(AttributeEvent e) { }
		public void attributeValueChanged(AttributeEvent e) {
			Attribute<?> attr = e.getAttribute();
			if (attr == StdAttr.FACING) {
				fireChanged();
			}
		}
	}

	private EventSourceWeakSupport<CircuitPinListener> listeners;
	private MyComponentListener myComponentListener;
	private Set<Instance> pins;

	public CircuitPins() {
		listeners = new EventSourceWeakSupport<CircuitPinListener>();
		myComponentListener = new MyComponentListener();
		pins = new HashSet<Instance>();
	}

	public void addPinListener(CircuitPinListener l) {
		listeners.add(l);
	}
	
	public void removePinListener(CircuitPinListener l) {
		listeners.remove(l);
	}
	
	void fireChanged() {
		for (CircuitPinListener listener : listeners) {
			listener.pinsChanged();
		}
	}

	public void addPin(Component pinComponent) {
		Instance instance = Instance.getInstanceFor(pinComponent);
		boolean added = pins.add(instance);
		if (added) {
			pinComponent.getAttributeSet().addAttributeListener(myComponentListener);
			pinComponent.addComponentListener(myComponentListener);
			fireChanged();
		}
	}

	public void removePin(Component pinComponent) {
		Instance instance = Instance.getInstanceFor(pinComponent);
		boolean removed = pins.remove(instance);
		if (removed) {
			pinComponent.getAttributeSet().removeAttributeListener(myComponentListener);
			pinComponent.removeComponentListener(myComponentListener);
			fireChanged();
		}
	}
	
	public Collection<Instance> getPins() {
		return new ArrayList<Instance>(pins);
	}
}
