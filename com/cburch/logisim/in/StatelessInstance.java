package com.cburch.logisim.in;

import com.cburch.logisim.util.StringGetter;

public abstract class StatelessInstance extends ManagedInstance<StatelessState> {
	public StatelessInstance(String staticName, StringGetter displayName) {
		super(staticName, displayName);
	}
	
	public StatelessState createState() {
		return new StatelessState(this);
	}
}
