package com.cburch.logisim.in;

import com.cburch.draw.util.EditableLabel;
import com.cburch.logisim.tools.key.KeyConfigurator;

/** Methods relating to interacting with an instance. */
public interface InstanceInteractive {
	public EditableLabel getLabel();
	public KeyConfigurator getKeyConfigurator();
}
