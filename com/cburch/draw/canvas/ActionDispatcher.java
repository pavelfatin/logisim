package com.cburch.draw.canvas;

import com.cburch.draw.undo.Action;

public interface ActionDispatcher {
	public void doAction(Action action);
}
