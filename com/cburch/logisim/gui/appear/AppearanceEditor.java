package com.cburch.logisim.gui.appear;

import javax.swing.JScrollPane;

import com.cburch.draw.gui.AttributeManager;
import com.cburch.draw.model.DrawingAttributeSet;
import com.cburch.draw.toolbar.ToolbarModel;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.main.AttributeTable;
import com.cburch.logisim.gui.main.ZoomModel;
import com.cburch.logisim.proj.Project;

public class AppearanceEditor extends JScrollPane {
	private DrawingAttributeSet attrs;
	private AppearanceCanvas canvas;
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
		setViewportView(canvas);
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
	
	public void setCircuit(Project proj, Circuit circuit) {
		canvas.setCircuit(proj, circuit);
	}
}
