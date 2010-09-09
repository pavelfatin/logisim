package com.cburch.draw.undo;

import java.util.EventListener;

public interface UndoLogListener extends EventListener {
	public void undoLogChanged(UndoLogEvent e);
}
