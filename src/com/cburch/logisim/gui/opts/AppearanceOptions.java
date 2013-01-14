/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.util.TableLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AppearanceOptions extends OptionsPanel {
    private JCheckBox detailedAppearanceCheckBox = new JCheckBox();

    public AppearanceOptions(OptionsFrame frame) {
        super(frame);

        setLayout(new TableLayout(1));
        add(detailedAppearanceCheckBox);

        MyListener listener = new MyListener();

        detailedAppearanceCheckBox.addActionListener(listener);
        frame.getOptions().getAttributeSet().addAttributeListener(listener);

        AttributeSet attrs = getOptions().getAttributeSet();
        listener.loadShowLabels(attrs.getValue(Options.detailed_appearance_attr));
    }

    @Override
    public String getTitle() {
        return Strings.get("appearanceTitle");
    }

    @Override
    public String getHelpText() {
        return Strings.get("appearanceHelp");
    }

    @Override
    public void localeChanged() {
        detailedAppearanceCheckBox.setText(Strings.get("detailedAppearance"));
    }

    private class MyListener implements ActionListener, AttributeListener {
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();
            if (source == detailedAppearanceCheckBox) {
                AttributeSet attrs = getOptions().getAttributeSet();
                Boolean val = Boolean.valueOf(detailedAppearanceCheckBox.isSelected());
                getProject().doAction(OptionsActions.setAttribute(attrs, Options.detailed_appearance_attr, val));
            }
        }

        public void attributeListChanged(AttributeEvent e) {}

        public void attributeValueChanged(AttributeEvent e) {
            Attribute<?> attr = e.getAttribute();
            Object val = e.getValue();
            if (attr == Options.detailed_appearance_attr) {
                loadShowLabels((Boolean) val);
            }
        }

        private void loadShowLabels(Boolean val) {
            detailedAppearanceCheckBox.setSelected(val);
        }
    }
}
