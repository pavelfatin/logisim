package com.cburch.draw.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import com.cburch.draw.actions.ModelAddAction;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.model.DrawAttr;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;

abstract class RectangularTool extends AbstractTool {
	private boolean active;
	private Location dragStart;
	private Location dragEnd;
	private int lastMouseX;
	private int lastMouseY;
	
	public RectangularTool() {
		active = false;
	}
	
	public abstract CanvasObject createShape(int x, int y, int w, int h);
	public abstract void drawShape(Graphics g, int x, int y, int w, int h);
	public abstract void fillShape(Graphics g, int x, int y, int w, int h);
	
	@Override
	public Cursor getCursor(Canvas canvas) {
		return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.ATTRS_FILL;
	}
	
	@Override
	public void toolDeselected(Canvas canvas) {
		active = false;
		repaintArea(canvas);
	}
	
	@Override
	public void mousePressed(Canvas canvas, MouseEvent e) {
		Location loc = Location.create(e.getX(), e.getY());
		dragStart = loc;
		dragEnd = loc;
		lastMouseX = loc.getX();
		lastMouseY = loc.getY();
		active = canvas.getModel() != null;
		repaintArea(canvas);
	}
	
	@Override
	public void mouseDragged(Canvas canvas, MouseEvent e) {
		updateMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
	}
	
	@Override
	public void mouseReleased(Canvas canvas, MouseEvent e) {
		if(active) {
			updateMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
			active = false;
			Location start = dragStart;
			Location end = dragEnd;
			if(!start.equals(end)) {
				Bounds bds = Bounds.create(start).add(end);
				if(bds.getWidth() != 0 || bds.getWidth() != 0) {
					CanvasModel model = canvas.getModel();
					CanvasObject add = createShape(bds.getX(), bds.getY(),
							bds.getWidth(), bds.getHeight());
					canvas.doAction(new ModelAddAction(model, add));
					repaintArea(canvas);
				}
			}
		}
	}
	
	@Override
	public void keyPressed(Canvas canvas, KeyEvent e) {
		if(active && e.getKeyCode() == KeyEvent.VK_SHIFT) {
			updateMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
		}
	}
	
	@Override
	public void keyReleased(Canvas canvas, KeyEvent e) {
		if(active && e.getKeyCode() == KeyEvent.VK_SHIFT) {
			updateMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
		}
	}
	
	private void updateMouse(Canvas canvas, int mx, int my, int mods) {
		if(active) {
			int x1 = mx;
			int y1 = my;
			boolean shiftDown = (mods & MouseEvent.SHIFT_DOWN_MASK) != 0;
			if(shiftDown) {
				Location start = dragStart;
				int x0 = start.getX();
				int y0 = start.getY();
				int w = Math.abs(x0 - x1);
				int h = Math.abs(y0 - y1);
				if(w < h) {
					y1 = y1 < y0 ? y0 - w : y0 + w;
				} else {
					x1 = x1 < x0 ? x0 - h : x0 + h; 
				}
			}
			Location end = dragEnd;
			if(x1 != end.getX() || y1 != end.getY()) {
				dragEnd = Location.create(x1, y1);
				repaintArea(canvas);
			}
		}
		lastMouseX = mx;
		lastMouseY = my;
	}

	
	private void repaintArea(Canvas canvas) {
		canvas.repaint();
	}
	
	@Override
	public void draw(Canvas canvas, Graphics g) {
		if(active) {
			Location start = dragStart;
			Location end = dragEnd;
			int x = start.getX();
			int y = start.getY();
			int x1 = end.getX();
			int y1 = end.getY();
			int w;
			int h;
			if(x1 < x) {
				w = x - x1;
				x = x1;
			} else {
				w = x1 - x;
			}
			if(y1 < y) {
				h = y - y1;
				y = y1;
			} else {
				h = y1 - y;
			}
			g.setColor(Color.GRAY);
			drawShape(g, x, y, w, h);
		}
	}

}
