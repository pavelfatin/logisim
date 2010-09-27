package com.cburch.logisim.in;

import com.cburch.logisim.data.Value;

public abstract class State {
	private Instance<?> instance;
	
	public State(Instance<?> instance) {
		this.instance = instance;
	}
	
	public boolean isStateless() {
		return false;
	}
	
	public Instance<?> getInstance() {
		return instance;
	}
	
	public Value getPort(int index) {
		return null; // TODO
	}
	
	public void setPort(int index, Value value, int delay) {
		// TODO
	}
	
	public void schedulePropagate(int delay) {
		// TODO
	}
	
	public void addEdge(Port source, Port dest, int delay) {
		// TODO
	}
	
	public void removeEdge(Port source, Port dest) {
		// TODO
	}
	
	protected void repropagate() {
		// TODO
	}
}
