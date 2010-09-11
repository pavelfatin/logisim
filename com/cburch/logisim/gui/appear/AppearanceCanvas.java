package com.cburch.logisim.gui.appear;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.cburch.draw.canvas.ActionDispatcher;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasModelEvent;
import com.cburch.draw.canvas.CanvasModelListener;
import com.cburch.draw.canvas.CanvasTool;
import com.cburch.draw.undo.Action;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.CanvasPaneContents;
import com.cburch.logisim.gui.generic.GridPainter;
import com.cburch.logisim.proj.Project;

public class AppearanceCanvas extends Canvas
		implements CanvasPaneContents, ActionDispatcher {
	private static final int BOUNDS_BUFFER = 70;
		// pixels shown in canvas beyond outermost boundaries
	private static final int THRESH_SIZE_UPDATE = 10;
		// don't bother to update the size if it hasn't changed more than this
	
	private class Listener implements CanvasModelListener {
		public void modelChanged(CanvasModelEvent event) {
			computeSize(false);
		}
	}

	private Project proj;
	private CircuitState circuitState;
	private Listener listener;
	private GridPainter grid;
	private CanvasPane canvasPane;
	private Bounds oldPreferredSize;
	private LayoutPopupManager popupManager;
	
	public AppearanceCanvas() {
		this.grid = new GridPainter(this);
		this.listener = new Listener();
		this.oldPreferredSize = null;
		setSelection(new AppearanceSelection());
	}
	
	@Override
	public void setTool(CanvasTool value) {
		hidePopup();
		super.setTool(value);
	}
	
	public void setCanvasModel(CanvasModel value) {
		CanvasModel oldModel = super.getModel();
		if (oldModel != null) {
			oldModel.removeCanvasModelListener(listener);
		}
		super.setModel(value, this);
		if (value != null) {
			value.addCanvasModelListener(listener);
		}
	}
	
	public void setCircuit(Project proj, CircuitState circuitState) {
		this.proj = proj;
		this.circuitState = circuitState;
		Circuit circuit = circuitState.getCircuit();
		super.setModel(circuit.getAppearance(), this);
	}
	
	Circuit getCircuit() {
		return circuitState.getCircuit();
	}
	
	CircuitState getCircuitState() {
		return circuitState;
	}
	
	GridPainter getGridPainter() {
		return grid;
	}
	
	@Override
	public void doAction(Action canvasAction) {
		Circuit circuit = circuitState.getCircuit();
		proj.doAction(new CanvasActionAdapter(circuit, canvasAction));
	}
	
	@Override
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		grid.paintGrid(g);
	}
	
	@Override
	protected void paintForeground(Graphics g) {
		double zoom = grid.getZoomFactor();
		Graphics gScaled = g.create();
		if (zoom != 1.0 && zoom != 0.0 && gScaled instanceof Graphics2D) {
			((Graphics2D) gScaled).scale(zoom, zoom);
		}
		super.paintForeground(gScaled);
		gScaled.dispose();
	}
	
	@Override
	public void repaintCanvasCoords(int x, int y, int width, int height) {
		double zoom = grid.getZoomFactor();
		if (zoom != 1.0) {
			x = (int) (x * zoom - 1);
			y = (int) (y * zoom - 1);
			width = (int) (width * zoom + 4);
			height = (int) (height * zoom + 4);
		}
		super.repaintCanvasCoords(x, y, width, height);
	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		repairEvent(e, grid.getZoomFactor());
		super.processMouseEvent(e);
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		repairEvent(e, grid.getZoomFactor());
		super.processMouseMotionEvent(e);
	}
	
	private void hidePopup() {
		LayoutPopupManager man = popupManager;
		if (man != null) {
			man.hideCurrentPopup();
		}
	}
	
	private void repairEvent(MouseEvent e, double zoom) {
		if (zoom != 1.0) {
			int oldx = e.getX();
			int oldy = e.getY();
			int newx = (int) Math.round(e.getX() / zoom);
			int newy = (int) Math.round(e.getY() / zoom);
			e.translatePoint(newx - oldx, newy - oldy);
		}
	}

	private void computeSize(boolean immediate) {
		hidePopup();
		Bounds bounds;
		CircuitState circState = circuitState;
		if (circState == null) {
			bounds = Bounds.create(0, 0, 50, 50);
		} else {
			bounds = circState.getCircuit().getAppearance().getAbsoluteBounds();
		}
		int width = bounds.getX() + bounds.getWidth() + BOUNDS_BUFFER;
		int height = bounds.getY() + bounds.getHeight() + BOUNDS_BUFFER;
		Dimension dim;
		if (canvasPane == null) {
			dim = new Dimension(width, height);
		} else {
			dim = canvasPane.supportPreferredSize(width, height);
		}
		if (!immediate) {
			Bounds old = oldPreferredSize;
			if (old != null
					&& Math.abs(old.getWidth() - dim.width) < THRESH_SIZE_UPDATE
					&& Math.abs(old.getHeight() - dim.height) < THRESH_SIZE_UPDATE) {
				return;
			}
		}
		oldPreferredSize = Bounds.create(0, 0, dim.width, dim.height);
		setPreferredSize(dim);
		revalidate();
	}

	//
	// CanvasPaneContents methods
	//
	public void setCanvasPane(CanvasPane value) {
		canvasPane = value;
		computeSize(true);
		popupManager = new LayoutPopupManager(value, this);
	}

	public void recomputeSize() {
		computeSize(true);
		repaint();
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return canvasPane.supportScrollableBlockIncrement(visibleRect, orientation, direction);
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return canvasPane.supportScrollableUnitIncrement(visibleRect, orientation, direction);
	}

}
