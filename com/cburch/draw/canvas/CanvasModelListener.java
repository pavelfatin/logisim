package com.cburch.draw.canvas;

import java.util.EventListener;

public interface CanvasModelListener extends EventListener {
	public void modelChanged(CanvasModelEvent event);
}
