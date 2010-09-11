package com.cburch.draw.model;

import java.awt.Graphics;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Location;

public class Rectangle extends Rectangular {
	public Rectangle(int x, int y, int w, int h) {
		super(x, y, w, h);
	}
	
	public String getDisplayName() {
		return Strings.get("shapeRect");
	}
	
	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createRectangle(doc, this);
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.getFillAttributes(getPaintType());
	}
	
	@Override
	protected boolean contains(int x, int y, int w, int h, Location q) {
		int qx = q.getX();
		int qy = q.getY();
		return qx >= x && qx < x + w && qy >= y && qy < y + h;
	}
	
	@Override
	public void draw(Graphics g, int x, int y, int w, int h) {
		if(setForFill(g)) g.fillRect(x, y, w, h);
		if(setForStroke(g)) g.drawRect(x, y, w, h);
	}
}
