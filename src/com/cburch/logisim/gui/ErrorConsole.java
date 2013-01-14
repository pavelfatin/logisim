/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui;

import com.cburch.logisim.proj.Projects;

import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ErrorConsole extends OutputStream {
    private static final String TITLE = "Error";
    private static final Font FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final int COLUMNS = 50;
    private static final int ROWS = 20;

    private JTextArea textArea;

    private void write(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doWrite(s);
            }
        });
    }

    private void doWrite(String s) {
        if (textArea == null) {
            textArea = createTextArea(s);
            show(textArea);
            textArea = null;
        } else {
            textArea.append(s);
        }
    }

    private static JTextArea createTextArea(String s) {
        JTextArea area = new JTextArea(s);
        area.setColumns(COLUMNS);
        area.setRows(ROWS);
        area.setFont(FONT);
        area.setEditable(false);
        return area;
    }

    private static void show(JTextArea area) {
        JScrollPane scrollPane = new JScrollPane(area);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(Projects.getTopFrame(), scrollPane, TITLE, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void write(int b) throws IOException {
        write(new String(new char[]{(char) b}));
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(new String(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        write(new String(b, off, len));
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}
