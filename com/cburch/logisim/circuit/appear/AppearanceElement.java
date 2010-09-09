package com.cburch.logisim.circuit.appear;

import java.awt.Graphics;
import java.util.Collections;
import java.util.List;

import com.cburch.draw.model.DrawingMember;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

public abstract class AppearanceElement extends DrawingMember {
	private Location location;
	
	public AppearanceElement(Location location) {
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return Collections.emptyList();
	}

	@Override
	public <V> V getValue(Attribute<V> attr) {
		return null;
	}
	
	@Override
	public boolean canRemove() {
		return false;
	}
	
	@Override
	public boolean canMoveHandle(Location handle) {
		return false;
	}

	@Override
	public void moveHandle(Location handle, int dx, int dy) {
		// nothing to do
	}

	@Override
	protected void updateValue(Attribute<?> attr, Object value) {
		// nothing to do
	}

	@Override
	public void translate(int dx, int dy) {
		location = location.translate(dx, dy);
	}

	public boolean contains(Location loc) {
		int dx = loc.getX() - location.getX();
		int dy = loc.getY() - location.getY();
		int r = getRadius();
		return dx * dx + dy * dy < r * r;
	}

	public Bounds getBounds() {
		int r = getRadius();
		return Bounds.create(location.getX() - r, location.getY() - r,
				2 * r, 2 * r);
	}

	public List<Location> getHandles(Location handle, int dx, int dy) {
		Location loc = location;
		int r = getRadius();
		return UnmodifiableList.create(new Location[] {
				loc.translate(-r, -r), loc.translate(r, -r),
				loc.translate(r, r), loc.translate(-r, r),
		});
	}

	public abstract void paint(Graphics g, Location handle, int handleDx, int handleDy);

	public abstract String getDisplayName();
	
	protected abstract int getRadius();
}
