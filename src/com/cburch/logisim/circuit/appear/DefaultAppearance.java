/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit.appear;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.shapes.Curve;
import com.cburch.draw.shapes.DrawAttr;
import com.cburch.draw.shapes.Rectangle;
import com.cburch.draw.shapes.Text;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.StdAttr;

class DefaultAppearance {
	private static final int OFFS = 50;
	
	private DefaultAppearance() { }
	
	private static class CompareLocations implements Comparator<Instance> {
		private boolean byX;
		
		CompareLocations(boolean byX) {
			this.byX = byX;
		}
		
		public int compare(Instance a, Instance b) {
			Location aloc = a.getLocation();
			Location bloc = b.getLocation();
			if (byX) {
				int ax = aloc.getX();
				int bx = bloc.getX();
				if (ax != bx) {
					return ax < bx ? -1 : 1;
				}
			} else {
				int ay = aloc.getY();
				int by = bloc.getY();
				if (ay != by) {
					return ay < by ? -1 : 1;
				}
			}
			return aloc.compareTo(bloc);
		}
	}

	static void sortPinList(List<Instance> pins, Direction facing) {
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			Comparator<Instance> sortHorizontal = new CompareLocations(true);
			Collections.sort(pins, sortHorizontal);
		} else {
			Comparator<Instance> sortVertical = new CompareLocations(false);
			Collections.sort(pins, sortVertical);
		}
	}

	
	public static List<CanvasObject> build(Collection<Instance> pins) {
		return build("", pins, false);
	}

	public static List<CanvasObject> buildDetailed(String name, Collection<Instance> pins) {
		return build(name, pins, true);
	}

	private static List<CanvasObject> build(String name, Collection<Instance> pins, boolean showLabels) {
		Map<Direction,List<Instance>> edge;
		edge = new HashMap<Direction,List<Instance>>();
		edge.put(Direction.NORTH, new ArrayList<Instance>());
		edge.put(Direction.SOUTH, new ArrayList<Instance>());
		edge.put(Direction.EAST, new ArrayList<Instance>());
		edge.put(Direction.WEST, new ArrayList<Instance>());
		for (Instance pin : pins) {
			Direction pinFacing = pin.getAttributeValue(StdAttr.FACING);
			Direction pinEdge = pinFacing.reverse();
			List<Instance> e = edge.get(pinEdge);
			e.add(pin);
		}
		
		for (Map.Entry<Direction, List<Instance>> entry : edge.entrySet()) {
			sortPinList(entry.getValue(), entry.getKey());
		}

		int numNorth = edge.get(Direction.NORTH).size();
		int numSouth = edge.get(Direction.SOUTH).size();
		int numEast = edge.get(Direction.EAST).size();
		int numWest = edge.get(Direction.WEST).size();
		int maxVert = Math.max(numNorth, numSouth);
		int maxHorz = Math.max(numEast, numWest);

		Rectangle2D nameBounds = boundsOf(name);

		boolean nameVisible = showLabels && !name.isEmpty();
		int nameOffset = nameVisible ? 15 + (int) nameBounds.getHeight() : 0;

		int dx = 10;
		int dy = showLabels ? 20 : 10;

		int offsNorth = computeOffset(numNorth, numSouth, maxHorz, dx, false);
		int offsSouth = computeOffset(numSouth, numNorth, maxHorz, dx, false);
		int offsEast = nameOffset + computeOffset(numEast, numWest, maxVert, dy, !showLabels);
		int offsWest = nameOffset + computeOffset(numWest, numEast, maxVert, dy, !showLabels);

		int maxLabelWidth = Math.max((int) nameBounds.getWidth(),
				maxLabelPairWidth(edge.get(Direction.WEST), edge.get(Direction.EAST)));

        int additionalWidth = showLabels ? 10 + multiple(maxLabelWidth, 10) : 0;

        int width = computeDimension(maxVert, maxHorz, dx) + additionalWidth;
		int height = nameOffset + computeDimension(maxHorz, maxVert, dy);

		// compute position of anchor relative to top left corner of box
		int ax;
		int ay;
		if (numEast > 0) { // anchor is on east side
			ax = width;
			ay = offsEast;
		} else if (numNorth > 0) { // anchor is on north side
			ax = offsNorth;
			ay = 0;
		} else if (numWest > 0) { // anchor is on west side
			ax = 0;
			ay = offsWest;
		} else if (numSouth > 0) { // anchor is on south side
			ax = offsSouth;
			ay = height;
		} else { // anchor is top left corner
			ax = 0;
			ay = 0;
		}
		
		// place rectangle so anchor is on the grid
		int rx = OFFS + (9 - (ax + 9) % 10);
		int ry = OFFS + (9 - (ay + 9) % 10);
		
		Location e0 = Location.create(rx + (width - 8) / 2, ry + 1);
		Location e1 = Location.create(rx + (width + 8) / 2, ry + 1);
		Location ct = Location.create(rx + width / 2, ry + 11);
		Curve notch = new Curve(e0, e1, ct);
		notch.setValue(DrawAttr.STROKE_WIDTH, Integer.valueOf(2));
		notch.setValue(DrawAttr.STROKE_COLOR, Color.GRAY);
		Rectangle rect = new Rectangle(rx, ry, width, height);
		rect.setValue(DrawAttr.STROKE_WIDTH, Integer.valueOf(2));

		List<CanvasObject> ret = new ArrayList<CanvasObject>();
		ret.add(notch);
		ret.add(rect);

		if (nameVisible) {
			int hx = rx + (width - (int) nameBounds.getWidth()) / 2;
			int hy = ry + 20;
			Text text = new Text(hx, hy, name);
			text.getLabel().setFont(DrawAttr.DEFAULT_FONT.deriveFont(Font.BOLD));
			ret.add(text);
		}

		placePins(ret, edge.get(Direction.WEST),
				rx,             ry + offsWest,  0, dy);
		placePins(ret, edge.get(Direction.EAST),
				rx + width,     ry + offsEast,  0, dy);
		placePins(ret, edge.get(Direction.NORTH),
				rx + offsNorth, ry,            dx,  0);
		placePins(ret, edge.get(Direction.SOUTH),
				rx + offsSouth, ry + height,   dx,  0);
		ret.add(new AppearanceAnchor(Location.create(rx + ax, ry + ay)));

		if (showLabels) {
			placePinLabels(ret, edge.get(Direction.WEST),
					rx + 10, ry + offsWest + 5, 0, dy, DrawAttr.ALIGN_LEFT);

			placePinLabels(ret, edge.get(Direction.EAST),
					rx + width - 10, ry + offsEast + 5, 0, dy, DrawAttr.ALIGN_RIGHT);
		}

		return ret;
	}

    private static int multiple(int value, int n) {
        return (int) Math.ceil((float) value / (float) n) * n;
	}

	private static int maxLabelPairWidth(List<Instance> xs, List<Instance> ys) {
		return xs.size() >= ys.size()
				? maxLabelPairWidth0(xs, ys)
				: maxLabelPairWidth0(ys, xs);
	}

	private static int maxLabelPairWidth0(List<Instance> xs, List<Instance> ys) {
		int maxWidth = 0;

		Iterator<Instance> ysIterator = ys.iterator();

		for (Instance x : xs) {
			int xLabelWidth = labelWidthOf(x);
			int yLabelWidth = ysIterator.hasNext() ? labelWidthOf(ysIterator.next()) : 0;

			int pairWidth = xLabelWidth + yLabelWidth;

			maxWidth = Math.max(maxWidth, pairWidth);
		}

		return maxWidth;
	}

	private static int labelWidthOf(Instance pin) {
		return (int) boundsOf(pin.getAttributeSet().getValue(StdAttr.LABEL)).getWidth();
	}

	private static Rectangle2D boundsOf(String text) {
		Font font = DrawAttr.DEFAULT_FONT;
		FontRenderContext context = new FontRenderContext(null, false, false);
		return font.getStringBounds(text, context);
	}

	private static int computeDimension(int maxThis, int maxOthers, int delta) {
		if (maxThis < 3) {
			return maxThis == 0 ? 30 : delta * 3;
		} else if (maxOthers == 0) {
			return delta * maxThis;
		} else {
			return delta * maxThis + 10;
		}
	}

	private static int computeOffset(int numFacing, int numOpposite, int maxOthers, int delta, boolean center) {
		int maxThis = Math.max(numFacing, numOpposite);
		int maxOffs;
		switch (maxThis) {
		case 0:
		case 1:
			maxOffs = (maxOthers == 0 ? 15 : 10);
			break;
		case 2:
			maxOffs = 10;
			break;
		default:
			maxOffs = (maxOthers == 0 ? 5 : 10);
		}
		return center ? maxOffs + delta * ((maxThis - numFacing) / 2) : maxOffs;
	}
	
	private static void placePins(List<CanvasObject> dest, List<Instance> pins,
			int x, int y, int dx, int dy) {
		for (Instance pin : pins) {
			dest.add(new AppearancePort(Location.create(x, y), pin));
			x += dx;
			y += dy;
		}
	}

	private static void placePinLabels(List<CanvasObject> dest, List<Instance> pins,
									   int x, int y, int dx, int dy, AttributeOption align) {
		for (Instance pin : pins) {
			String label = pin.getAttributeSet().getValue(StdAttr.LABEL);
			if (!label.isEmpty()) {
				Text text = new Text(x, y, label);
				text.getAttributeSet().setValue(DrawAttr.ALIGNMENT, align);
				dest.add(text);
			}
			x += dx;
			y += dy;
		}
	}
}
