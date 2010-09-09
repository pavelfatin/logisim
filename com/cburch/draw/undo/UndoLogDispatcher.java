package com.cburch.draw.undo;

import com.cburch.draw.canvas.ActionDispatcher;

public class UndoLogDispatcher implements ActionDispatcher {
	private UndoLog log;
	
	public UndoLogDispatcher(UndoLog log) {
		this.log = log;
	}
	
	public void doAction(Action action) {
		log.doAction(action);
	}
}
