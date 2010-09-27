package com.cburch.logisim.in;

public class StatelessState extends State {
	public StatelessState(Instance<?> instance) {
		super(instance);
	}
	
	@Override
	public boolean isStateless() {
		return true;
	}
}
