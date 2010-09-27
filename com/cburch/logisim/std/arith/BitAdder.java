/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.arith;

import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.in.ManagedInstance;
import com.cburch.logisim.in.Painter;
import com.cburch.logisim.in.Port;
import com.cburch.logisim.in.StatelessInstance;
import com.cburch.logisim.in.StatelessState;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.IntegerConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;

public class BitAdder extends StatelessInstance {
	static final Attribute<Integer> NUM_INPUTS
		= Attributes.forIntegerRange("inputs", Strings.getter("gateInputsAttr"), 1, 32);
	
	private BitWidth width;
	private int inputs;
	
	public BitAdder() {
		super("BitAdder", Strings.getter("bitAdderComponent"));
		width = BitWidth.create(8);
		inputs = 1;
		
		shareIconName("bitadder.gif");
		shareAttributes(new Attribute[] { StdAttr.WIDTH, NUM_INPUTS });
		shareAttributeAffects(StdAttr.WIDTH, PORTS);
		shareAttributeAffects(NUM_INPUTS, PORTS | BOUNDS);
		shareKeyConfigurator(new IntegerConfigurator(NUM_INPUTS, 1, 32, 0),
				new BitWidthConfigurator(StdAttr.WIDTH));
	}
	
	@Override
	protected Object retrieveValue(Attribute<?> attr) {
		if (attr == StdAttr.WIDTH) return width;
		if (attr == NUM_INPUTS) return Integer.valueOf(inputs);
		return null;
	}
	
	@Override
	protected void updateValue(Attribute<?> attr, Object value) {
		if (attr == StdAttr.WIDTH) width = (BitWidth) value;
		if (attr == NUM_INPUTS) inputs = ((Integer) value).intValue();
	}
	
	@Override
	protected Bounds computeBounds() {
		int inputs = this.inputs;
		int h = Math.max(40, 10 * inputs);
		int y = inputs < 4 ? 20 : (((inputs - 1) / 2) * 10 + 5);
		return Bounds.create(-40, -y, 40, h);
	}

	@Override
	protected Port[] computePorts() {
		BitWidth inWidth = width;
		int inputs = this.inputs;
		int outWidth = computeOutputBits(inWidth.getWidth(), inputs);

		int y;
		int dy = 10;
		switch (inputs) {
		case 1: y = 0; break;
		case 2: y = -10; dy = 20; break;
		case 3: y = -10; break;
		default: y = ((inputs - 1) / 2) * -10;
		}

		Port[] ps = new Port[inputs + 1];
		ps[0]   = newOutput(0, 0, BitWidth.create(outWidth));
		for (int i = 0; i < inputs; i++) {
			ps[i + 1] = newInput(-40, y + i * dy, inWidth);
		}
		return ps;
	}
	
	private int computeOutputBits(int width, int inputs) {
		int maxBits = width * inputs;
		int outWidth = 1;
		while ((1 << outWidth) < maxBits) outWidth++;
		return outWidth;
	}

	@Override
	public void propagate(StatelessState state) {
		int width = this.width.getWidth();
		int inputs = this.inputs;

		// compute the number of 1 bits
		int minCount = 0; // number that are definitely 1
		int maxCount = 0; // number that are definitely not 0 (incl X/Z)
		for (int i = 1; i <= inputs; i++) {
			Value v = state.getPort(i);
			Value[] bits = v.getAll();
			for (int j = 0; j < bits.length; j++) {
				Value b = bits[j];
				if (b == Value.TRUE) minCount++;
				if (b != Value.FALSE) maxCount++;
			}
		}

		// compute which output bits should be error bits
		int unknownMask = 0;
		for (int i = minCount + 1; i <= maxCount; i++) {
			unknownMask |= (minCount ^ i);
		}
		
		Value[] out = new Value[computeOutputBits(width, inputs)];
		for (int i = 0; i < out.length; i++) {
			if (((unknownMask >> i) & 1) != 0) {
				out[i] = Value.ERROR;
			} else if (((minCount >> i) & 1) != 0) {
				out[i] = Value.TRUE;
			} else {
				out[i] = Value.FALSE;
			}
		}

		int delay = out.length * Adder.PER_DELAY;
		state.setPort(0, Value.create(out), delay);
	}
	
	@Override
	public void paintInstance(Painter painter, StatelessState state) {
		Graphics g = painter.getGraphics();
		painter.drawBounds();
		painter.drawPorts(state);
		
		GraphicsUtil.switchToWidth(g, 2);
		int x = -10;
		int y = 0;
		g.drawLine(-12, -5, -12,  5);
		g.drawLine( -8, -5,  -8,  5);
		g.drawLine(-15, -2,  -5, -2);
		g.drawLine(-15,  2,  -5,  2);
	}
}
