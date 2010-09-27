package com.cburch.logisim.in;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.cburch.draw.util.EditableLabel;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.KeyConfigurator;
import com.cburch.logisim.util.Icons;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.UnmodifiableList;

class SharedData {
	private String saveName;
	private StringGetter displayName;
	private List<Attribute<?>> attrs;
	private Map<Attribute<?>, Integer> attrsAffect;
	private Icon icon;
	private KeyConfigurator keyConfigurator;
	private Attribute<String> labelTextAttr;
	private Attribute<Font> labelFontAttr;
	private Attribute<Color> labelColorAttr;
	
	public SharedData(String saveName, StringGetter displayName) {
		this.saveName = saveName;
		this.displayName = displayName;
		this.attrs = Collections.emptyList();
		this.attrsAffect = new HashMap<Attribute<?>, Integer>();
		this.keyConfigurator = null;
	}
	
	public String getSaveName() {
		return saveName;
	}
	
	public StringGetter getDisplayName() {
		return displayName;
	}
	
	public List<Attribute<?>> getAttributes() {
		return attrs;
	}
	
	public void setAttributes(Attribute<?>[] attrs) {
		this.attrs = new UnmodifiableList<Attribute<?>>(attrs);
	}
	
	public void setAffected(Attribute<?> attr, int changeMask) {
		if (changeMask == 0) {
			attrsAffect.remove(attr);
		} else {
			attrsAffect.put(attr, Integer.valueOf(changeMask));
		}
	}
	
	public int getAffected(Attribute<?> attr) {
		Integer ret = attrsAffect.get(attr);
		return ret == null ? 0 : ret.intValue();
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public void setIcon(Icon value) {
		icon = value;
	}
	
	public void setIconName(String value) {
		icon = Icons.getIcon(value);
	}
	
	public KeyConfigurator getKeyConfigurator() {
		return keyConfigurator;
	}
	
	public void setKeyConfigurator(KeyConfigurator value) {
		keyConfigurator = value;
	}
	
	public void setLabelAttributes(Attribute<String> labelAttr,
			Attribute<Font> fontAttr, Attribute<Color> colorAttr) {
		labelTextAttr = labelAttr;
		labelFontAttr = fontAttr;
		labelColorAttr = colorAttr;
	}
	
	public EditableLabel createLabel() {
		return new EditableLabel(0, 0, "", StdAttr.DEFAULT_LABEL_FONT);
	}
	
	public Object retrieveLabelValue(EditableLabel label, Attribute<?> attr) {
		if (attr == labelTextAttr) return label.getText();
		if (attr == labelFontAttr) return label.getFont();
		if (attr == labelColorAttr) return label.getColor();
		return null;
	}
	
	public boolean updateLabelValue(EditableLabel label, Attribute<?> attr,
			Object value) {
		boolean found = false;
		if (attr == labelTextAttr) {
			found = true;
			label.setText((String) value);
		}
		if (attr == labelFontAttr) {
			found = true;
			label.setFont((Font) value);
		}
		if (attr == labelColorAttr) {
			found = true;
			label.setColor((Color) value);
		}
		return found;
	}
}
