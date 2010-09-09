package com.cburch.draw.canvas;

import java.util.EventListener;

public interface SelectionListener extends EventListener {
	public void selectionChanged(SelectionEvent e);
}
