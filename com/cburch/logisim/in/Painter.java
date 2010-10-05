package com.cburch.logisim.in;

import java.awt.Graphics;

import com.cburch.logisim.data.Direction;

public interface Painter {
	public Graphics getGraphics();
	public boolean shouldShowColor();
	public boolean shouldShowState();
	
	public void drawBounds();
	public void drawLabel();
	public void drawPorts();
	public void drawPort(int index);
	public void drawPort(int index, String label, Direction dir);
	public void drawClock(int index, Direction dir);
}
