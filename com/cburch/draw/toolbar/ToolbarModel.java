package com.cburch.draw.toolbar;

import java.util.List;

public interface ToolbarModel {
	public void addToolbarModelListener(ToolbarModelListener listener);
	public void removeToolbarModelListener(ToolbarModelListener listener);
	public List<ToolbarItem> getItems();
	public boolean isSelected(ToolbarItem item);
	public void itemSelected(ToolbarItem item);
}
