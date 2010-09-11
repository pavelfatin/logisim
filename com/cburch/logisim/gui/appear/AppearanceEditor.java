package com.cburch.logisim.gui.appear;

import com.cburch.draw.gui.AttributeManager;
import com.cburch.draw.model.DrawingAttributeSet;
import com.cburch.draw.toolbar.ToolbarModel;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.generic.AttributeTable;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.ZoomModel;
import com.cburch.logisim.proj.Project;

public class AppearanceEditor {
	private DrawingAttributeSet attrs;
	private AppearanceCanvas canvas;
	private CanvasPane canvasPane;
	private AppearanceToolbarModel toolbarModel;
	private AttributeManager attributeManager;
	private ZoomModel zoomModel;
	
	public AppearanceEditor() {
		attrs = new DrawingAttributeSet();
		canvas = new AppearanceCanvas();
		toolbarModel = new AppearanceToolbarModel(canvas, attrs);
		canvas.setTool(toolbarModel.getFirstTool());
		zoomModel = new AppearanceZoomModel();
		canvas.getGridPainter().setZoomModel(zoomModel);
		attributeManager = null;
		canvasPane = new CanvasPane(canvas);
		canvasPane.setZoomModel(zoomModel);
	}
	
	public CanvasPane getCanvasPane() {
		return canvasPane;
	}
	
	public ToolbarModel getToolbarModel() {
		return toolbarModel;
	}
	
	public ZoomModel getZoomModel() {
		return zoomModel;
	}
	
	public AttributeSet getAttributeSet() {
		return attrs;
	}
	
	public AttributeManager getAttributeManager(AttributeTable table) {
		AttributeManager ret = attributeManager;
		if (ret == null) {
			ret = new AttributeManager(canvas, table, attrs);
			attributeManager = ret;
		}
		return ret;
	}
	
	public void setCircuit(Project proj, CircuitState circuitState) {
		canvas.setCircuit(proj, circuitState);
	}
}
