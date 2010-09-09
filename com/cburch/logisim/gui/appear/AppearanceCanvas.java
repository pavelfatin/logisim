package com.cburch.logisim.gui.appear;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.cburch.draw.canvas.ActionDispatcher;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.undo.Action;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.gui.main.GridPainter;
import com.cburch.logisim.proj.Project;

public class AppearanceCanvas extends Canvas implements ActionDispatcher {
	private Project proj;
	private Circuit circuit;
	private GridPainter grid;
	
	public AppearanceCanvas() {
		this.grid = new GridPainter(this);
		setSelection(new AppearanceSelection());
	}
	
	public void setCircuit(Project proj, Circuit circuit) {
		this.proj = proj;
		this.circuit = circuit;
		super.setModel(circuit.getAppearance(), this);
	}
	
	GridPainter getGridPainter() {
		return grid;
	}
	
	@Override
	public void doAction(Action canvasAction) {
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
	protected void processMouseEvent(MouseEvent e) {
		repairEvent(e, grid.getZoomFactor());
		super.processMouseEvent(e);
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		repairEvent(e, grid.getZoomFactor());
		super.processMouseMotionEvent(e);
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
}
