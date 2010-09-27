package com.cburch.logisim.std.memory;

import com.cburch.logisim.in.Instance;
import com.cburch.logisim.in.StateClocked;

public class RegisterState extends StateClocked {
	private int data;
	
	public RegisterState(Instance<?> instance) {
		super(instance);
	}
	
	public int getData() {
		return data;
	}
	
	public void setData(int value) {
		if (data != value) {
			data = value;
			repropagate();
		}
	}
}
