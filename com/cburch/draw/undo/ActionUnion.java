package com.cburch.draw.undo;

class ActionUnion extends Action {
	Action first;
	Action second;

	ActionUnion(Action first, Action second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean isModification() {
		return first.isModification() || second.isModification();
	}

	@Override
	public String getName() { return first.getName(); }

	@Override
	public void doIt() {
		first.doIt();
		second.doIt();
	}

	@Override
	public void undo() {
		second.undo();
		first.undo();
	}
}
