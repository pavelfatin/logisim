/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.in;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.StringGetter;

public class Port {
	public static final int PASSIVE = 0;
	public static final int INPUT = 1;
	public static final int OUTPUT = 2;
	public static final int INOUT = 3;

	private Instance<?> instance;
	private Location loc;
	private BitWidth width;
	private int type;
	private boolean exclude;
	private StringGetter toolTip;
	
	Port(Instance<?> instance, Location loc, int type, BitWidth width) {
		this (instance, loc, type, width, defaultExclusive(type), null);
	}
	
	private Port(Instance<?> instance, Location loc, int type, BitWidth width,
			boolean exclusive, StringGetter toolTip) {
		this.instance = instance;
		this.loc = loc;
		this.type = type;
		this.width = width;
		this.exclude = exclusive;
		this.toolTip = toolTip;
	}
	
	public Port cloneToolTip(StringGetter value) {
		return new Port(instance, loc, type, width, exclude, value);
	}
	
	public Port cloneExclusive(boolean value) {
		return new Port(instance, loc, type, width, value, toolTip);
	}
	
	public String getToolTip() {
		StringGetter getter = toolTip;
		return getter == null ? null : getter.get();
	}
	
	private static boolean defaultExclusive(int type) {
		return type == OUTPUT;
	}
}
