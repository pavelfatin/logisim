package com.cburch.logisim.gui.main;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class MainPanel extends JPanel {
	public static final String LAYOUT = "layout";
	public static final String APPEARANCE = "appearance";
	
	private ArrayList<ChangeListener> listeners;
	private String current;
	
	public MainPanel() {
		super(new CardLayout());
		listeners = new ArrayList<ChangeListener>();
		current = "";
	}
	
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	public void addView(String name, Component comp) {
		add(comp, name);
	}
	
	public String getView() {
		return current;
	}
	
	public void setView(String choice) {
		if (choice == null) choice = "";
		String oldChoice = current;
		if (!oldChoice.equals(choice)) {
			current = choice;
			((CardLayout) getLayout()).show(this, choice);
			ChangeEvent e = new ChangeEvent(this);
			for (ChangeListener listener : listeners) {
				listener.stateChanged(e);
			}
		}
	}

}
