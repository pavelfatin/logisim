package com.cburch.draw.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import com.cburch.draw.actions.ModelDeleteHandleAction;
import com.cburch.draw.actions.ModelInsertHandleAction;
import com.cburch.draw.actions.ModelMoveHandleAction;
import com.cburch.draw.actions.ModelRemoveAction;
import com.cburch.draw.actions.ModelTranslateAction;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.canvas.Selection;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.Icons;

class SelectTool extends AbstractTool {
	private static final int IDLE = 0;
	private static final int MOVE_ALL = 1;
	private static final int RECT_SELECT = 2;
	private static final int RECT_TOGGLE = 3;
	private static final int MOVE_HANDLE = 4;
	
	private static final int DRAG_TOLERANCE = 2;
	private static final int HANDLE_SIZE = 8;
	
	private int curAction;
	private Location dragStart;
	private Location dragEnd;
	private int lastMouseX;
	private int lastMouseY;
	
	public SelectTool() {
		curAction = IDLE;
		dragStart = Location.create(0, 0);
		dragEnd = dragStart;
	}
	
	@Override
	public Icon getIcon() {
		return Icons.getIcon("select.gif");
	}

	@Override
	public Cursor getCursor(Canvas canvas) {
		return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return Collections.emptyList();
	}
	
	@Override
	public void toolSelected(Canvas canvas) {
		canvas.getSelection().clearSelected();
		repaintArea(canvas);
	}
	
	@Override
	public void toolDeselected(Canvas canvas) {
		canvas.getSelection().clearSelected();
		repaintArea(canvas);
	}
	
	@Override
	public void mousePressed(Canvas canvas, MouseEvent e) {
		int mx = e.getX();
		int my = e.getY();
		boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
		dragStart = Location.create(mx, my);
		dragEnd = dragStart;
		lastMouseX = mx;
		lastMouseY = my;
		Selection selection = canvas.getSelection();
		selection.setHandleSelected(null, null);
		
		// see whether user is pressing within an existing handle
		int halfSize = HANDLE_SIZE / 2;
		CanvasObject clicked = null;
		for (CanvasObject shape : selection.getSelected()) {
			List<Location> handles = shape.getHandles(null, 0, 0);
			for (Location loc : handles) {
				int dx = loc.getX() - mx;
				int dy = loc.getY() - my;
				if(dx >= -halfSize && dx <= halfSize
						&& dy >= -halfSize && dy <= halfSize) {
					if (shape.canMoveHandle(loc)) {
						curAction = MOVE_HANDLE;
						selection.setHandleSelected(shape, loc);
						repaintArea(canvas);
						return;
					} else if (clicked == null) {
						clicked = shape;
					}
				}
			}
		}

		// see whether the user is clicking within a shape
		if (clicked == null) {
			clicked = canvas.getModel().getObjectAt(e.getX(), e.getY());
		}
		if(clicked != null) {
			if(shift && selection.isSelected(clicked)) {
				selection.setSelected(clicked, false);
				curAction = IDLE;
			} else {
				if(!shift && !selection.isSelected(clicked)) {
					selection.clearSelected();
				}
				selection.setSelected(clicked, true);
				selection.setMovingShapes(selection.getSelected(), 0, 0);
				curAction = MOVE_ALL;
			}
			repaintArea(canvas);
			return;
		}
		
		if(shift) {
			curAction = RECT_TOGGLE;
		} else {
			selection.clearSelected();
			curAction = RECT_SELECT;
		}
		repaintArea(canvas);
	}
	
	@Override
	public void mouseDragged(Canvas canvas, MouseEvent e) {
		setMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
	}
	
	@Override
	public void mouseReleased(Canvas canvas, MouseEvent e) {
		setMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
		
		CanvasModel model = canvas.getModel();
		Selection selection = canvas.getSelection();
		Set<CanvasObject> selected = selection.getSelected();
		int action = curAction;
		curAction = IDLE;
		
		Location start = dragStart;
		int x1 = e.getX();
		int y1 = e.getY();
		switch(action) {
		case MOVE_ALL:
			Location delta = selection.getMovingDelta();
			int dx = delta.getX();
			int dy = delta.getY();
			if(Math.abs(dx) + Math.abs(dy) >= DRAG_TOLERANCE) {
				canvas.doAction(new ModelTranslateAction(model, selected, dx, dy));
			}
			break;
		case MOVE_HANDLE:
			delta = selection.getHandleDelta();
			dx = delta.getX();
			dy = delta.getY();
			CanvasObject hanShape = selection.getHandleShape();
			Location handle = selection.getHandleLocation();
			if(Math.abs(dx) + Math.abs(dy) >= DRAG_TOLERANCE && hanShape != null) {
				canvas.doAction(new ModelMoveHandleAction(model, hanShape,
						handle, dx, dy));
			}
			break;
		case RECT_SELECT:
			Bounds bds = Bounds.create(start).add(x1, y1);
			for (CanvasObject shape : canvas.getModel().getObjectsIn(bds)) {
				selection.setSelected(shape, true);
			}
			break;
		case RECT_TOGGLE:
			bds = Bounds.create(start).add(x1, y1);
			for (CanvasObject shape : canvas.getModel().getObjectsIn(bds)) {
				selection.setSelected(shape, !selected.contains(shape));
			}
			break;
		}
		selection.clearDrawsSuppressed();
		repaintArea(canvas);
	}
	
	@Override
	public void keyPressed(Canvas canvas, KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_SHIFT:
			if(curAction != IDLE) {
				setMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
			}
			break;
		case KeyEvent.VK_INSERT:
			Selection selection = canvas.getSelection();
			CanvasObject shape = selection.getHandleShape();
			Location handle = selection.getHandleLocation();
			if(shape != null && shape.canInsertHandle(handle)) {
				CanvasModel model = canvas.getModel();
				canvas.doAction(new ModelInsertHandleAction(model, shape, handle));
				repaintArea(canvas);
				e.consume();
			}
			break;
		case KeyEvent.VK_DELETE:
			selection = canvas.getSelection();
			shape = selection.getHandleShape();
			handle = selection.getHandleLocation();
			if(shape != null && shape.canDeleteHandle(handle)) {
				CanvasModel model = canvas.getModel();
				canvas.doAction(new ModelDeleteHandleAction(model, shape, handle));
				repaintArea(canvas);
				e.consume();
			}
			break;
		}
	}
	
	@Override
	public void keyReleased(Canvas canvas, KeyEvent e) {
		if(curAction != IDLE && e.getKeyCode() == KeyEvent.VK_SHIFT) {
			setMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
		}
	}
	
	@Override
	public void keyTyped(Canvas canvas, KeyEvent e) {
		char ch = e.getKeyChar();
		Selection selected = canvas.getSelection();
		if((ch == '\u0008' || ch == '\u007F') && !selected.isEmpty()) {
			boolean found = false;
			for (CanvasObject shape : selected.getSelected()) {
				if (shape.canRemove()) {
					found = true;
					break;
				}
			}
			if (found) {
				e.consume();
				CanvasModel model = canvas.getModel();
				canvas.doAction(new ModelRemoveAction(model, selected.getSelected()));
				selected.clearSelected();
				repaintArea(canvas);
			}
		} else if(ch == '\u001b' && !selected.isEmpty()) {
			selected.clearSelected();
			repaintArea(canvas);
		}
	}
	
	
	private void setMouse(Canvas canvas, int mx, int my, int mods) {
		lastMouseX = mx;
		lastMouseY = my;
		boolean shift = (mods & MouseEvent.SHIFT_DOWN_MASK) != 0
			&& curAction == MOVE_ALL;
		Location newEnd = shift ? LineTool.snapTo4Cardinals(dragStart, mx, my)
				: Location.create(mx, my);
		if(!newEnd.equals(dragEnd)) {
			dragEnd = newEnd;

			Location start = dragStart;
			int dx = newEnd.getX() - start.getX();
			int dy = newEnd.getY() - start.getY();
			if(Math.abs(dx) + Math.abs(dy) < DRAG_TOLERANCE) { dx = 0; dy = 0; }

			switch(curAction) {
			case MOVE_HANDLE:
				canvas.getSelection().setHandleDelta(dx, dy);
				break;
			case MOVE_ALL:
				canvas.getSelection().setMovingDelta(dx, dy);
				break;
			}
			repaintArea(canvas);
		}
	}

	private void repaintArea(Canvas canvas) {
		canvas.repaint();
	}
	
	@Override
	public void draw(Canvas canvas, Graphics g) {
		Selection selection = canvas.getSelection();
		int action = curAction;

		Location start = dragStart;
		Location end = dragEnd;
		int dx;
		int dy;
		switch (action) {
		case MOVE_ALL:
			Location delta = selection.getMovingDelta();
			dx = delta.getX();
			dy = delta.getY();
			break;
		case MOVE_HANDLE:
			delta = selection.getHandleDelta();
			dx = delta.getX();
			dy = delta.getY();
			break;
		default:
			dx = end.getX() - start.getX();
			dy = end.getY() - start.getY();
		}
		if(Math.abs(dx) + Math.abs(dy) < DRAG_TOLERANCE) { dx = 0; dy = 0; }

		int size = HANDLE_SIZE;
		int offs = HANDLE_SIZE / 2;
		CanvasObject hanShape = selection.getHandleShape();
		Location handle = selection.getHandleLocation();
		for (CanvasObject d : selection.getSelected()) {
			List<Location> handles;
			if(action == MOVE_HANDLE && d == hanShape) {
				handles = d.getHandles(handle, dx, dy);
			} else {
				handles = d.getHandles(null, 0, 0);
			}
			for (Location loc : handles) {
				int x = loc.getX();
				int y = loc.getY();
				if(action == MOVE_ALL) { x += dx; y += dy; }
				if(d == hanShape && loc.equals(handle)) {
					g.fillRect(x - offs, y - offs, size + 1, size + 1);
				} else {
					g.clearRect(x - offs, y - offs, size, size);
					g.drawRect(x - offs, y - offs, size, size);
				}
			}
		}
		
		switch(action) {
		case RECT_SELECT:
		case RECT_TOGGLE:
			int x0 = start.getX();
			int y0 = start.getY();
			int x1 = end.getX();
			int y1 = end.getY();
			g.setColor(Color.gray);
			if(x1 < x0) { int t = x0; x0 = x1; x1 = t; }
			if(y1 < y0) { int t = y0; y0 = y1; y1 = t; }
			g.drawRect(x0, y0, x1 - x0, y1 - y0);
			break;
		}
	}
}
