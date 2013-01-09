/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import com.cburch.draw.model.CanvasObject;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.appear.CircuitAppearance;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;

import java.util.ArrayList;

public class GenerateDetailedAppearanceAction extends Action {
	private Circuit circuit;
	private ArrayList<CanvasObject> old;
	private boolean wasDefault;

	public GenerateDetailedAppearanceAction(Circuit circuit) {
		this.circuit = circuit;
	}

	@Override
	public String getName() {
		return Strings.get("generateDetailedAppearanceAction");
	}

	@Override
	public void doIt(Project proj) {
		CircuitAppearance appear = circuit.getAppearance();
		wasDefault = appear.isDefaultAppearance();
		old = new ArrayList<CanvasObject>(appear.getObjectsFromBottom());
		appear.generateDetailedAppearance();
	}

	@Override
	public void undo(Project proj) {
		CircuitAppearance appear = circuit.getAppearance();
		appear.setObjectsForce(old);
		appear.setDefaultAppearance(wasDefault);
	}
}
