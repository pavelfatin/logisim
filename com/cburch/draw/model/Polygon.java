package com.cburch.draw.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.GeneralPath;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Location;

class Polygon extends Poly {
	private GeneralPath path;
	private AttributeOption paintType;
	private Color fillColor;
	
	public Polygon(List<Location> locations) {
		super(locations);
		paintType = DrawAttr.PAINT_STROKE;
		fillColor = Color.WHITE;
	}
	
	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createPolygon(doc, this);
	}
	
	public String getDisplayName() {
		return Strings.get("shapePolygon");
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.getFillAttributes(paintType);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == DrawAttr.PAINT_TYPE) {
			return (V) paintType;
		} else if(attr == DrawAttr.FILL_COLOR) {
			return (V) fillColor;
		} else {
			return super.getValue(attr);
		}
	}
	
	@Override
	public void updateValue(Attribute<?> attr, Object value) {
		if (attr == DrawAttr.PAINT_TYPE) {
			paintType = (AttributeOption) value;
			fireAttributeListChanged();
		} else if(attr == DrawAttr.FILL_COLOR) {
			fillColor = (Color) value;
		} else {
			super.updateValue(attr, value);
		}
	}
	
	@Override
	public boolean contains(Location loc) {
		if(path.contains(loc.getX(), loc.getY())) return true;
		int width = getStrokeWidth();
		return ptBorderDistSq(loc) < (width * width) / 4; 
	}

	@Override
	protected void recomputeBounds() {
		super.recomputeBounds();
		List<Location> locs = getHandles(null, 0, 0);
		GeneralPath newPath = new GeneralPath();
		if (locs.size() > 0) {
			boolean first = true;
			for (Location loc : locs) {
				if (first) {
					newPath.moveTo(loc.getX(), loc.getY());
					first = false;
				} else {
					newPath.lineTo(loc.getX(), loc.getY());
				}
			}
		}
		path = newPath;
	}
	
	@Override
	public void draw(Graphics g, int[] xs, int[] ys) {
		if (setForFill(g)) g.fillPolygon(xs, ys, xs.length);
		if (setForStroke(g)) g.drawPolygon(xs, ys, xs.length);
	}
}