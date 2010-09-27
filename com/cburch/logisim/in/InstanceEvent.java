package com.cburch.logisim.in;

import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;

public class InstanceEvent {
	private Instance<?> instance;
	private int changeMask;
	private Attribute<?> attr;
	private Object oldValue;
	private Bounds oldBounds;
	private List<Port> oldPorts;
	
	public InstanceEvent(Instance<?> instance, int changeMask, Attribute<?> attr,
			Object oldValue, Bounds oldBounds, List<Port> oldPorts) {
		this.instance = instance;
		this.changeMask = changeMask;
		this.oldBounds = oldBounds;
		this.oldPorts = oldPorts;
	}
	
	public Instance<?> getInstance() {
		return instance;
	}
	
	public boolean hasChanged(int changeMask) {
		return (this.changeMask & changeMask) != 0;
	}
	
	public Attribute<?> getAttribute() {
		return attr;
	}
	
	public Object getOldValue() {
		return oldValue;
	}
	
	public Bounds getOldBounds() {
		return oldBounds;
	}
	
	public List<Port> getOldPorts() {
		return oldPorts;
	}
}
