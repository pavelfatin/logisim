/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.io.IOException;
import java.io.OutputStream;

public class CompoundOutputStream extends OutputStream {
    private OutputStream streamA;
    private OutputStream streamB;

    public CompoundOutputStream(OutputStream a, OutputStream b) {
        streamA = a;
        streamB = b;
    }

    @Override
    public void write(int b) throws IOException {
        streamA.write(b);
        streamB.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        streamA.write(b);
        streamB.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        streamA.write(b, off, len);
        streamB.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        streamA.flush();
        streamB.flush();
    }

    @Override
    public void close() throws IOException {
        streamA.close();
        streamB.close();
    }
}
