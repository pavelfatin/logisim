/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.in.ManagedInstance;
import com.cburch.logisim.in.Painter;
import com.cburch.logisim.in.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

public class Register extends ManagedInstance<RegisterState> {
	private static final int DELAY = 8;
	private static final int OUT = 0;
	private static final int IN  = 1;
	private static final int CK  = 2;
	private static final int CLR = 3;
	private static final int EN  = 4;
	
	private BitWidth width;
	private AttributeOption trigger;

	public Register() {
		super("Register", Strings.getter("registerComponent"));
		width = BitWidth.create(8);
		trigger = StdAttr.TRIG_RISING;
		
		shareIconName("register.gif");
		shareAttributes(new Attribute[] {
				StdAttr.WIDTH, StdAttr.TRIGGER,
				StdAttr.LABEL, StdAttr.LABEL_FONT
			});
		shareAttributeAffects(StdAttr.WIDTH, STATE);
		shareAttributeAffects(StdAttr.TRIGGER, STATE);
		shareLabelAttributes(StdAttr.LABEL, StdAttr.LABEL_FONT);
		shareKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));
		sharePoker(RegisterPoker.class);
		shareLogger(RegisterLogger.class);
	}
	
	@Override
	protected Object retrieveValue(Attribute<?> attr) {
		if (attr == StdAttr.WIDTH) return width;
		if (attr == StdAttr.TRIGGER) return trigger;
		return null;
	}
	
	@Override
	protected void updateValue(Attribute<?> attr, Object value) {
		if (attr == StdAttr.WIDTH) width = (BitWidth) value;
		if (attr == StdAttr.TRIGGER) trigger = (AttributeOption) value;
	}
	
	@Override
	protected Bounds computeBounds() {
		return Bounds.create(-30, -20, 30, 40);
	}
	
	@Override
	protected Port[] computePorts() { 
		Port[] ps = new Port[5];
		ps[OUT] = newOutput(  0,  0, width)
			.cloneToolTip(Strings.getter("registerQTip"));
		ps[IN]  = newInput(-30,  0, width)
			.cloneToolTip(Strings.getter("registerDTip"));
		ps[CK]  = newInput(-20, 20, BitWidth.ONE)
			.cloneToolTip(Strings.getter("registerClkTip"));
		ps[CLR] = newInput(-10, 20, BitWidth.ONE)
			.cloneToolTip(Strings.getter("registerClrTip"));
		ps[EN]  = newInput(-30, 10, BitWidth.ONE)
			.cloneToolTip(Strings.getter("registerEnableTip"));
		return ps;
	}
	
	@Override
	protected void computeLabel() {
		Bounds bds = getBounds();
		setLabelLocation(bds.getX() + bds.getWidth() / 2, bds.getY() - 3,
				GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
	}
	
	@Override
	public RegisterState createState() {
		return new RegisterState(this);
	}

	@Override
	public void propagate(RegisterState state) {
		BitWidth dataWidth = this.width;
		AttributeOption trigger = this.trigger;
		
		boolean triggered = state.setClock(state.getPort(CK), trigger.getValue());
		if (state.getPort(CLR) == Value.TRUE) {
			state.setData(0);
		} else if (triggered && state.getPort(EN) != Value.FALSE) {
			Value in = state.getPort(IN);
			if (in.isFullyDefined()) state.setData(in.toIntValue());
		} 

		state.setPort(OUT, Value.createKnown(dataWidth, state.getData()), DELAY);
	}

	@Override
	public void paintInstance(Painter painter, RegisterState state) {
		Graphics g = painter.getGraphics();
		Bounds bds = getBounds();
		int width = this.width.getWidth();

		// determine text to draw in label
		String a;
		String b = null;
		if (painter.shouldShowState()) {
			int val = state.getData();
			String str = StringUtil.toHexString(width, val);
			if (str.length() <= 4) {
				a = str;
			} else {
				int split = str.length() - 4;
				a = str.substring(0, split);
				b = str.substring(split);
			}
		} else {
			a = Strings.get("registerLabel");
			b = Strings.get("registerWidthLabel", "" + width);
		}

		// draw boundary, label
		painter.drawBounds();
		painter.drawLabel();

		// draw input and output ports
		if (b == null) {
			painter.drawPort(IN,  "D", Direction.EAST);
			painter.drawPort(OUT, "Q", Direction.WEST);
		} else {
			painter.drawPort(IN);
			painter.drawPort(OUT);
		}
		g.setColor(Color.GRAY);
		painter.drawPort(CLR, "0", Direction.SOUTH);
		painter.drawPort(EN, Strings.get("memEnableLabel"), Direction.EAST);
		g.setColor(Color.BLACK);
		painter.drawClock(CK, Direction.NORTH);

		// draw contents
		if (b == null) {
			GraphicsUtil.drawText(g, a, bds.getX() + 15, bds.getY() + 4,
					GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
		} else {
			GraphicsUtil.drawText(g, a, bds.getX() + 15, bds.getY() + 3,
					GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
			GraphicsUtil.drawText(g, b, bds.getX() + 15, bds.getY() + 15,
					GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
		}
	}
}