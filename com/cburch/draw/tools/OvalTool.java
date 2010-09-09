package com.cburch.draw.tools;

import java.awt.Graphics;

import javax.swing.Icon;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.model.Drawables;
import com.cburch.draw.model.DrawingAttributeSet;
import com.cburch.logisim.util.Icons;

class OvalTool extends RectangularTool {
	private DrawingAttributeSet attrs;
	
	public OvalTool(DrawingAttributeSet attrs) {
		this.attrs = attrs;
	}
	
	@Override
	public Icon getIcon() {
		return Icons.getIcon("drawoval.gif");
	}

	@Override
	public CanvasObject createShape(int x, int y, int w, int h) {
		return Drawables.createOval(x, y, w, h, attrs);
	}

	@Override
	public void drawShape(Graphics g, int x, int y, int w, int h) {
		g.drawOval(x, y, w, h);
	}

	@Override
	public void fillShape(Graphics g, int x, int y, int w, int h) {
		g.fillOval(x, y, w, h);
	}
}
