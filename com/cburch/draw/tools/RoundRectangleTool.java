package com.cburch.draw.tools;

import java.awt.Graphics;
import java.util.List;

import javax.swing.Icon;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.model.Drawables;
import com.cburch.draw.model.DrawAttr;
import com.cburch.draw.model.DrawingAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.util.Icons;

class RoundRectangleTool extends RectangularTool {
	private DrawingAttributeSet attrs;
	
	public RoundRectangleTool(DrawingAttributeSet attrs) {
		this.attrs = attrs;
	}
	
	@Override
	public Icon getIcon() {
		return Icons.getIcon("drawrrct.gif");
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.getRoundRectAttributes(attrs.getValue(DrawAttr.PAINT_TYPE));
	}

	@Override
	public CanvasObject createShape(int x, int y, int w, int h) {
		return Drawables.createRoundRectangle(x, y, w, h, attrs);
	}

	@Override
	public void drawShape(Graphics g, int x, int y, int w, int h) {
		g.drawRoundRect(x, y, w, h, 10, 10);
	}

	@Override
	public void fillShape(Graphics g, int x, int y, int w, int h) {
		g.fillRoundRect(x, y, w, h, 10, 10);
	}
}
