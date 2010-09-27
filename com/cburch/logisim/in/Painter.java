package com.cburch.logisim.in;

import java.awt.Graphics;

public interface Painter {
	public Graphics getGraphics();
	public boolean shouldShowColor();
	public boolean shouldShowState();
}
