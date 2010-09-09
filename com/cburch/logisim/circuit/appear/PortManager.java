package com.cburch.logisim.circuit.appear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.StdAttr;

class PortManager {
	private PortManager() { }
	
	public static void updatePorts(CircuitAppearance appear,
			Collection<Instance> circuitPins) {
		// Find the current objects corresponding to pins
		Map<Instance, AppearancePort> oldObjects;
		oldObjects = new HashMap<Instance, AppearancePort>();
		AppearanceOrigin origin = null;
		for (CanvasObject o : appear.getObjects()) {
			if (o instanceof AppearancePort) {
				AppearancePort port = (AppearancePort) o;
				oldObjects.put(port.getPin(), port);
			} else if (o instanceof AppearanceOrigin) {
				origin = (AppearanceOrigin) o; 
			}
		}
		
		// ensure we have the origin in the circuit
		if (origin == null) {
			for (CanvasObject o : DefaultAppearance.build(circuitPins)) {
				if (o instanceof AppearanceOrigin) {
					origin = (AppearanceOrigin) o;
				}
			}
			if (origin == null) {
				origin = new AppearanceOrigin(Location.create(100, 100));
			}
			appear.addObjects(Collections.singleton(origin));
		}
		
		// Now find which of these pins are not found in circuit,
		// and which pins in circuit are not represented here
		Set<Instance> unused = new HashSet<Instance>(oldObjects.keySet());
		Map<Location, Instance> unrep = new TreeMap<Location, Instance>();
		for (Instance pin : circuitPins) {
			if (oldObjects.containsKey(pin)) {
				unused.remove(pin);
			} else {
				unrep.put(pin.getLocation(), pin);
			}
		}
		
		// Remove any pins not represented in circuit
		if (unused.size() > 0) {
			List<CanvasObject> toRemove;
			toRemove = new ArrayList<CanvasObject>(unused.size());
			for (Instance pin : unused) {
				CanvasObject o = oldObjects.remove(pin);
				toRemove.add(o);
			}
			appear.removeObjects(toRemove);
		}
		
		// Add any pins not represented in appearance
		if (unrep.size() > 0) {
			List<CanvasObject> toAdd;
			toAdd = new ArrayList<CanvasObject>(unrep.size());
			for (Instance pin : unrep.values()) {
				Location loc = computeDefaultLocation(appear, pin, oldObjects);
				CanvasObject o = new AppearancePort(loc, pin);
				toAdd.add(o);
			}
			appear.addObjects(toAdd);
		}
	}
	
	private static Location computeDefaultLocation(CircuitAppearance appear,
			Instance pin, Map<Instance, AppearancePort> others) {
		// Determine which locations are being used in canvas, and look for
		// which instances facing the same way in layout
		Set<Location> usedLocs = new HashSet<Location>();
		Map<Location, Instance> sameWay = new TreeMap<Location, Instance>();
		Direction facing = pin.getAttributeValue(StdAttr.FACING);
		for (Map.Entry<Instance, AppearancePort> entry : others.entrySet()) {
			Instance pin2 = entry.getKey();
			Location loc = entry.getValue().getLocation();
			usedLocs.add(loc);
			if (pin2.getAttributeValue(StdAttr.FACING) == facing) {
				sameWay.put(pin2.getLocation(), pin2);
			}
		}
		
		// if at least one faces the same way, place pin relative to that
		if (sameWay.size() > 0) {
			sameWay.put(pin.getLocation(), pin);
			boolean isFirst = false; 
			Instance neighbor = null; // (preferably previous in map)
			boolean atFirst = true;
			boolean found = false;
			for (Instance p : sameWay.values()) {
				if (p == pin) {
					found = true;
					isFirst = atFirst;
					if (!atFirst) break; // neighbor is previous
				} else {
					neighbor = p;
					if (found) break; // neighbor is next
				}
				atFirst = false;
			}
			int dx;
			int dy;
			if (facing == Direction.EAST || facing == Direction.WEST) {
				dx = 0;
				dy = isFirst? -10 : 10;
			} else {
				dx = isFirst ? -10 : 10;
				dy = 0;
			}
			Location loc = others.get(neighbor).getLocation();
			do {
				loc = loc.translate(dx, dy);
			} while (usedLocs.contains(loc));
			if (loc.getX() >= 0 && loc.getY() >= 0) {
				return loc;
			}
			do {
				loc = loc.translate(-dx, -dy);
			} while (usedLocs.contains(loc));
			return loc;
		}
		
		// otherwise place it on the boundary of the bounding rectangle
		Bounds bds = appear.getAbsoluteBounds();
		int x;
		int y;
		int dx = 0;
		int dy = 0;
		if (facing == Direction.EAST) { // on west side by default
			x = bds.getX() - 7;
			y = bds.getY() + 5;
			dy = 10;
		} else if (facing == Direction.WEST) { // on east side by default
			x = bds.getX() + bds.getWidth() - 3;
			y = bds.getY() + 5;
			dy = 10;
		} else if (facing == Direction.SOUTH) { // on north side by default
			x = bds.getX() + 5;
			y = bds.getY() - 7;
			dx = 10;
		} else { // on south side by default
			x = bds.getX() + 5;
			y = bds.getY() + bds.getHeight() - 3;
			dx = 10;
		}
		x = (x + 9) / 10 * 10; // round coordinates up to ensure they're on grid
		y = (y + 9) / 10 * 10;
		Location loc = Location.create(x, y);
		while (usedLocs.contains(loc)) {
			loc = loc.translate(dx, dy);
		}
		return loc;
	}
}
