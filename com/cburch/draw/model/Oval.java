package com.cburch.draw.model;

import java.awt.Graphics;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Location;

public class Oval extends Rectangular {
	public Oval(int x, int y, int w, int h) {
		super(x, y, w, h);
	}
	
	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createOval(doc, this);
	}
	
	public String getDisplayName() {
		return Strings.get("shapeOval");
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.ATTRS_FILL;
	}

	@Override
	protected boolean contains(int x, int y, int w, int h, Location q) {
		int qx = q.getX();
		int qy = q.getY();
		if(qx >= x && qx < x + w && qy >= y && qy < y + h) {
			double dx = qx - (x + 0.5 * w);
			double dy = qy - (y + 0.5 * h);
			double sum = (dx * dx) / (w * w) + (dy * dy) / (h * h);
			return sum <= 0.25;
		} else {
			return false;
		}
	}
	
	@Override
	public void draw(Graphics g, int x, int y, int w, int h) {
		if(setForFill(g)) g.fillOval(x, y, w, h);
		if(setForStroke(g)) g.drawOval(x, y, w, h);
	}
}
