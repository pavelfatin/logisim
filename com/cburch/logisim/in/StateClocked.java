package com.cburch.logisim.in;

import com.cburch.logisim.data.Value;

public class StateClocked extends State {
	public static final String RISING = "rising";
	public static final String FALLING = "falling";
	public static final String HIGH_LEVEL = "high";
	public static final String LOW_LEVEL = "low";
	
	private Value lastClock;
	
	public StateClocked(Instance<?> instance) {
		super(instance);
		lastClock = Value.ERROR;
	}

	public boolean setClock(Value clockValue, Object trigger) {
		if (lastClock.equals(clockValue)) {
			if (trigger == HIGH_LEVEL) return clockValue == Value.TRUE;
			if (trigger == LOW_LEVEL) return clockValue == Value.FALSE;
			return false;
		} else {
			lastClock = clockValue;
			if (trigger == HIGH_LEVEL || trigger == RISING) {
				return clockValue == Value.TRUE;
			}
			if (trigger == LOW_LEVEL || trigger == FALLING) {
				return clockValue == Value.FALSE;
			}
			return false;
		}
	}
}
