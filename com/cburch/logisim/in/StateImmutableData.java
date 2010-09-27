package com.cburch.logisim.in;

public class StateImmutableData<T> extends State {
	private T data;
	
	public StateImmutableData(Instance<?> instance, T init) {
		super(instance);
		data = init;
	}
	
	public T getData() {
		return data;
	}
	
	public void setData(T newData) {
		T oldData = data;
		boolean same = oldData == null ? newData == null : oldData.equals(newData);
		if (!same) {
			data = newData;
			repropagate();
		}
	}
}
