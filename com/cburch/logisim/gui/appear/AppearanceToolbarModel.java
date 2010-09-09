package com.cburch.logisim.gui.appear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.model.DrawingAttributeSet;
import com.cburch.draw.toolbar.AbstractToolbarModel;
import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.draw.tools.AbstractTool;
import com.cburch.draw.tools.ToolbarToolItem;

class AppearanceToolbarModel extends AbstractToolbarModel {
	private Canvas canvas;
	private List<ToolbarItem> items;
	
	public AppearanceToolbarModel(Canvas canvas, DrawingAttributeSet attrs) {
		this.canvas = canvas;
		ArrayList<ToolbarItem> rawItems = new ArrayList<ToolbarItem>();
		for (AbstractTool tool : AbstractTool.getTools(attrs)) {
			rawItems.add(new ToolbarToolItem(tool));
		}
		items = Collections.unmodifiableList(rawItems);
	}
	
	AbstractTool getFirstTool() {
		ToolbarToolItem item = (ToolbarToolItem) items.get(0);
		return item.getTool();
	}

	@Override
	public List<ToolbarItem> getItems() {
		return items;
	}
	
	@Override
	public boolean isSelected(ToolbarItem item) {
		if (item instanceof ToolbarToolItem) {
			AbstractTool tool = ((ToolbarToolItem) item).getTool();
			return tool == canvas.getTool();
		} else {
			return false;
		}
	}

	@Override
	public void itemSelected(ToolbarItem item) {
		if (item instanceof ToolbarToolItem) {
			AbstractTool tool = ((ToolbarToolItem) item).getTool();
			canvas.setTool(tool);
			fireToolbarAppearanceChanged();
		}
	}
}
