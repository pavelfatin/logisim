package com.cburch.draw.canvas;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import com.cburch.draw.undo.Action;


public class Canvas extends JComponent {
	public static final String TOOL_PROPERTY = "tool";
	public static final String MODEL_PROPERTY = "model";
	
	private CanvasModel model;
	private ActionDispatcher dispatcher;
	private CanvasListener listener;
	private Selection selection;
	
	public Canvas() {
		model = null;
		listener = new CanvasListener(this);
		selection = new Selection();
		
		addMouseListener(listener);
		addMouseMotionListener(listener);
		addKeyListener(listener);
		setPreferredSize(new Dimension(200, 200));
	}

	public CanvasModel getModel() {
		return model;
	}
	
	public CanvasTool getTool() {
		return listener.getTool();
	}
	
	public Selection getSelection() {
		return selection;
	}
	
	protected void setSelection(Selection value) {
		selection = value;
		repaint();
	}
	
	public void doAction(Action action) {
		dispatcher.doAction(action);
	}
	
	public void setModel(CanvasModel value, ActionDispatcher dispatcher) {
		CanvasModel oldValue = model;
		if(oldValue != value) {
			if(oldValue != null) oldValue.removeCanvasModelListener(listener);
			model = value;
			this.dispatcher = dispatcher;
			if(value != null) value.addCanvasModelListener(listener);
			selection.clearSelected();
			repaint();
			firePropertyChange(TOOL_PROPERTY, oldValue, value);
		}
	}
	
	public void setTool(CanvasTool value) {
		CanvasTool oldValue = listener.getTool();
		if(value != oldValue) {
			listener.setTool(value);
			firePropertyChange(TOOL_PROPERTY, oldValue, value);
		}
	}
	
	public void repaintCanvasCoords(int x, int y, int width, int height) {
		repaint(x, y, width, height);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		paintBackground(g);
		paintForeground(g);
	}
	
	protected void paintBackground(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
	}

	protected void paintForeground(Graphics g) {
		CanvasModel model = this.model;
		CanvasTool tool = listener.getTool();
		if(model != null) {
			Graphics dup = g.create();
			model.paint(g, selection);
			dup.dispose();
		}
		if(tool != null) {
			Graphics dup = g.create();
			tool.draw(this, dup);
			dup.dispose();
		}
	}
}
