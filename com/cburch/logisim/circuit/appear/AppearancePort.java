package com.cburch.logisim.circuit.appear;

import java.awt.Color;
import java.awt.Graphics;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.std.base.Pin;

public class AppearancePort extends AppearanceElement {
	private static final int RADIUS = 4;
	private static final int MINOR_RADIUS = 2;
	public static final Color COLOR = Color.BLUE;
	
	private Instance pin;
	
	public AppearancePort(Location location, Instance pin) {
		super(location);
		this.pin = pin;
	}

	@Override
	public String getDisplayName() {
		return Strings.get("circuitPort");
	}
	
	@Override
	public Element toSvgElement(Document doc) {
		Location loc = getLocation();
		Location pinLoc = pin.getLocation();
		Element ret = doc.createElement("circ-port");
		ret.setAttribute("x", "" + (loc.getX() - RADIUS));
		ret.setAttribute("y", "" + (loc.getY() - RADIUS));
		ret.setAttribute("width", "" + 2 * RADIUS);
		ret.setAttribute("height", "" + 2 * RADIUS);
		ret.setAttribute("pin", "" + pinLoc.getX() + "," + pinLoc.getY());
		return ret;
	}
	
	public Instance getPin() {
		return pin;
	}
	
	void setPin(Instance value) {
		pin = value;
	}
	
	@Override
	protected int getRadius() {
		return RADIUS;
	}

	@Override
	public void paint(Graphics g, Location handle, int handleDx, int handleDy) {
		Location location = getLocation();
		int x = location.getX();
		int y = location.getY();
		g.setColor(COLOR);
		if (Pin.FACTORY.isInputPin(pin)) {
			g.drawRect(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
		} else {
			g.drawOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
		}
		g.fillOval(x - MINOR_RADIUS, y - MINOR_RADIUS, 2 * MINOR_RADIUS, 2 * MINOR_RADIUS);
	}
}
