package com.cburch.draw.model;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.util.UnmodifiableList;

public class DrawAttr {
	public static final AttributeOption ALIGN_START
		= new AttributeOption("start", Strings.getter("alignStart"));
	public static final AttributeOption ALIGN_MIDDLE
		= new AttributeOption("middle", Strings.getter("alignMiddle"));
	public static final AttributeOption ALIGN_END
		= new AttributeOption("end", Strings.getter("alignEnd"));

	public static final Attribute<Font> FONT
		= Attributes.forFont("font", Strings.getter("attrFont"));
	public static final Attribute<AttributeOption> ALIGNMENT
		= Attributes.forOption("align", Strings.getter("attrAlign"),
			new AttributeOption[] { ALIGN_START, ALIGN_MIDDLE, ALIGN_END });
	public static final Attribute<Integer> STROKE_WIDTH
		= Attributes.forIntegerRange("stroke-width", Strings.getter("attrStrokeWidth"), 0, 8);
	public static final Attribute<Color> STROKE_COLOR
		= Attributes.forColor("stroke", Strings.getter("attrStroke"));
	public static final Attribute<Color> FILL_COLOR
		= Attributes.forColor("fill", Strings.getter("attrFill"));
	public static final Attribute<Integer> CORNER_RADIUS
		= Attributes.forIntegerRange("rx", Strings.getter("attrRx"), 1, 10);
	
	static final Color TRANSPARENT = new Color(255, 255, 255, 0);

	public static final List<Attribute<?>> ATTRS_TEXT // for text
		= createAttributes(new Attribute[] { FONT, ALIGNMENT, FILL_COLOR });
	public static final List<Attribute<?>> ATTRS_STROKE // for line, polyline
		= createAttributes(new Attribute[] { STROKE_WIDTH, STROKE_COLOR });
	public static final List<Attribute<?>> ATTRS_FILL // for rectangle, oval, polygon
		= createAttributes(new Attribute[] { STROKE_WIDTH, STROKE_COLOR,
				FILL_COLOR });
	public static final List<Attribute<?>> ATTRS_ROUND_RECT // for rounded rectangle
		= createAttributes(new Attribute[] { STROKE_WIDTH, STROKE_COLOR,
				FILL_COLOR, CORNER_RADIUS });
	static final List<Attribute<?>> ATTRS_ALL
		= createAttributes(new Attribute[] {
				FONT, ALIGNMENT,
				STROKE_WIDTH, STROKE_COLOR,
				FILL_COLOR, CORNER_RADIUS });
	static final List<Object> DEFAULTS_ALL
		= Arrays.asList(new Object[] {
				new Font("SansSerif", Font.PLAIN, 12), ALIGN_START,
				Integer.valueOf(1), Color.BLACK,
				TRANSPARENT, Integer.valueOf(10) });
	
	private static List<Attribute<?>> createAttributes(Attribute<?>[] values) {
		return new UnmodifiableList<Attribute<?>>(values);
	}
}
