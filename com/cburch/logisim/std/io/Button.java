/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.in.ManagedInstance;
import com.cburch.logisim.in.Painter;
import com.cburch.logisim.in.Port;
import com.cburch.logisim.in.StateImmutableData;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.DirectionConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.sun.corba.se.spi.orbutil.fsm.StateImpl;

public class Button extends ManagedInstance<StateImmutableData<Value>> {
	private static final int DEPTH = 3;
	
	private Direction facing;
	private Color color;
	private AttributeOption labelLoc;

	public Button() {
		super("Button", Strings.getter("buttonComponent"));		
		facing = Direction.EAST;
		color = Color.WHITE;
		labelLoc = Io.LABEL_CENTER;
		
		shareIconName("button.gif");
		shareAttributes(new Attribute[] {
				StdAttr.FACING, Io.ATTR_COLOR,
				StdAttr.LABEL, Io.ATTR_LABEL_LOC,
				StdAttr.LABEL_FONT, Io.ATTR_LABEL_COLOR
			});
		shareAttributeAffects(StdAttr.FACING, BOUNDS | LABEL);
		shareAttributeAffects(Io.ATTR_COLOR, APPEARANCE);
		shareAttributeAffects(Io.ATTR_LABEL_LOC, LABEL);
		shareLabelAttributes(StdAttr.LABEL, StdAttr.LABEL_FONT,
				Io.ATTR_LABEL_COLOR);
		shareKeyConfigurator(new DirectionConfigurator(StdAttr.FACING, 0));
		sharePoker(Poker.class);
		shareLogger(Logger.class);
	}
	
	@Override
	protected Object retrieveValue(Attribute<?> attr) {
		if (attr == StdAttr.FACING) return facing;
		if (attr == Io.ATTR_COLOR) return color;
		if (attr == Io.ATTR_LABEL_LOC) return labelLoc;
		return null;
	}
	
	@Override
	protected void updateValue(Attribute<?> attr, Object value) {
		if (attr == StdAttr.FACING) facing = (Direction) value;
		if (attr == Io.ATTR_COLOR) color = (Color) value;
		if (attr == Io.ATTR_LABEL_LOC) labelLoc = (AttributeOption) value;
	}
	
	@Override
	protected Bounds computeBounds() {
		Bounds base = Bounds.create(-20, -10, 20, 20);
		return base.rotate(Direction.EAST, facing, 0, 0);
	}

	@Override
	protected Port[] computePorts() {
		return new Port[] { newOutput(0, 0, BitWidth.ONE) };
	}

	@Override
	protected void computeLabel() {
		Direction facing = this.facing;
		Object labelLoc = this.labelLoc;

		Bounds bds = getBounds();
		int x = bds.getX() + bds.getWidth() / 2;
		int y = bds.getY() + bds.getHeight() / 2;
		int halign = GraphicsUtil.H_CENTER;
		int valign = GraphicsUtil.V_CENTER;
		if (labelLoc == Io.LABEL_CENTER) {
			x = bds.getX() + (bds.getWidth() - DEPTH) / 2;
			y = bds.getY() + (bds.getHeight() - DEPTH) / 2;
		} else if (labelLoc == Direction.NORTH) {
			y = bds.getY() - 2;
			valign = GraphicsUtil.V_BOTTOM;
		} else if (labelLoc == Direction.SOUTH) {
			y = bds.getY() + bds.getHeight() + 2;
			valign = GraphicsUtil.V_TOP;
		} else if (labelLoc == Direction.EAST) {
			x = bds.getX() + bds.getWidth() + 2;
			halign = GraphicsUtil.H_LEFT;
		} else if (labelLoc == Direction.WEST) {
			x = bds.getX() - 2;
			halign = GraphicsUtil.H_RIGHT;
		}
		if (labelLoc == facing) {
			if (labelLoc == Direction.NORTH || labelLoc == Direction.SOUTH) {
				x += 2;
				halign = GraphicsUtil.H_LEFT;
			} else {
				y -= 2;
				valign = GraphicsUtil.V_BOTTOM;
			}
		}
		
		setLabelLocation(x, y, halign, valign);
	}
	
	@Override
	public StateImmutableData<Value> createState() {
		return new StateImmutableData<Value>(this, Value.FALSE);
	}
	
	@Override
	public void propagate(StateImmutableData<Value> state) {
		state.setPort(0, state.getData(), 1);
	}
	
	@Override
	public void paintInstance(Painter painter, StateImmutableData<Value> state) {
		Bounds bds = getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int w = bds.getWidth();
		int h = bds.getHeight();
		
		Value val = painter.shouldShowState() ? state.getData() : Value.FALSE;
		Color col = color;
		if (painter.shouldShowColor()) {
			int lumin = (col.getRed() + col.getGreen() + col.getBlue()) / 3;
			color = new Color(lumin, lumin, lumin);
		}
		
		Graphics g = painter.getGraphics();
		int depress;
		if (val == Value.TRUE) {
			x += DEPTH;
			y += DEPTH;
			Object labelLoc = this.labelLoc;
			if (labelLoc == Io.LABEL_CENTER || labelLoc == Direction.NORTH
					|| labelLoc == Direction.WEST) {
				depress = DEPTH;
			} else {
				depress = 0;
			}
			
			Object facing = this.facing;
			if (facing == Direction.NORTH || facing == Direction.WEST) {
				GraphicsUtil.switchToWidth(g, Wire.WIDTH);
				g.setColor(Value.TRUE_COLOR);
				if (facing == Direction.NORTH) g.drawLine(0, 0, 0, 10);
				else                           g.drawLine(0, 0, 10, 0);
				GraphicsUtil.switchToWidth(g, 1);
			}
			
			g.setColor(color);
			g.fillRect(x, y, w - DEPTH, h - DEPTH);
			g.setColor(Color.BLACK);
			g.drawRect(x, y, w - DEPTH, h - DEPTH);
		} else {
			depress = 0;
			int[] xp = new int[] { x, x + w - DEPTH, x + w, x + w, x + DEPTH, x };
			int[] yp = new int[] { y, y, y + DEPTH, y + h, y + h, y + h - DEPTH };
			g.setColor(color.darker());
			g.fillPolygon(xp, yp, xp.length);
			g.setColor(color);
			g.fillRect(x, y, w - DEPTH, h - DEPTH);
			g.setColor(Color.BLACK);
			g.drawRect(x, y, w - DEPTH, h - DEPTH);
			g.drawLine(x + w - DEPTH, y + h - DEPTH, x + w, y + h);
			g.drawPolygon(xp, yp, xp.length);
		}
		
		g.translate(depress, depress);
		painter.drawLabel();
		g.translate(-depress, -depress);
		painter.drawPorts();
	}
	
	public static class Poker extends InstancePoker<StateImmutableData<Value>> {
		@Override
		public void mousePressed(MouseEvent e, StateImmutableData<Value> state) {
			state.setData(Value.TRUE);
		}
		
		@Override
		public void mouseReleased(MouseEvent e, StateImmutableData<Value> state) {
			state.setData(Value.FALSE);
		}
	}

	public static class Logger extends InstanceLogger<StateImmutableData<Value>> {
		@Override
		public String getLogName(StateImmutableData<Value> state, Object option) {
			Button instance = (Button) state.getInstance();
			return instance.label;
		}

		@Override
		public Value getLogValue(StateImmutableData<Value> state, Object option) {
			return state.getData();
		}
	}
}
