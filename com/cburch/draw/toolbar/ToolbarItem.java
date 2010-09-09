package com.cburch.draw.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public interface ToolbarItem {
	public boolean isSelectable();
	public void paintIcon(Component destination, Graphics g);
	public String getToolTip();
	public Dimension getDimension(Object orientation);
}
