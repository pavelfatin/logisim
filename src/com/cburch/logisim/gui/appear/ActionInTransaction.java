/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import com.cburch.logisim.proj.Action;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutator;
import com.cburch.logisim.circuit.CircuitTransaction;
import com.cburch.logisim.proj.Project;

import java.util.HashMap;
import java.util.Map;

public class ActionInTransaction extends Action {
	private Circuit circuit;
	private Action delegate;

	public ActionInTransaction(Circuit circuit, Action delegate) {
		this.circuit = circuit;
		this.delegate = delegate;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
    public void doIt(Project proj) {
        MyTransaction xn = new MyTransaction(proj, true);
        xn.execute();
    }

    @Override
    public void undo(Project proj) {
        MyTransaction xn = new MyTransaction(proj, false);
        xn.execute();
    }

	private class MyTransaction extends CircuitTransaction {
        private Project project;
        private boolean forward;
		
		MyTransaction(Project project, boolean forward) {
            this.project = project;
            this.forward = forward;
		}
		
		@Override
		protected Map<Circuit, Integer> getAccessedCircuits() {
			Map<Circuit, Integer> accessMap = new HashMap<Circuit, Integer>();
			for (Circuit supercirc : circuit.getCircuitsUsingThis()) {
				accessMap.put(supercirc, READ_WRITE);
			}
			return accessMap;
		}

		@Override
		protected void run(CircuitMutator mutator) {
			if (forward) {
				delegate.doIt(project);
			} else {
				delegate.undo(project);
			}
		}
		
	}
}
