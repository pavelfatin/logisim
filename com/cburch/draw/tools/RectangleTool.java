package com.cburch.draw.tools;

import java.awt.Graphics;
import java.util.List;

import javax.swing.Icon;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.model.DrawAttr;
import com.cburch.draw.model.Drawables;
import com.cburch.draw.model.DrawingAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.util.Icons;

class RectangleTool extends RectangularTool {
	private DrawingAttributeSet attrs;
	
	public RectangleTool(DrawingAttributeSet attrs) {
		this.attrs = attrs;
	}
	
	@Override
	public Icon getIcon() {
		return Icons.getIcon("drawrect.gif");
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.getFillAttributes(attrs.getValue(DrawAttr.PAINT_TYPE));
	}
	
	@Override
	public CanvasObject createShape(int x, int y, int w, int h) {
		return Drawables.createRectangle(x, y, w, h, attrs);
	}

	@Override
	public void drawShape(Graphics g, int x, int y, int w, int h) {
		g.drawRect(x, y, w, h);
	}

	@Override
	public void fillShape(Graphics g, int x, int y, int w, int h) {
		g.fillRect(x, y, w, h);
	}
}
