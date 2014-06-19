//
// Copyright (C) 2007 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * this is what we use for System.out and System.err, hence we go native as
 * quickly as possible, hence we don't need an underlying stream. It's already
 * slow enough as it is
 * 
 * NOTE - we have to intercept *everything* that might go to our base class,
 * since it is not initialized properly (we want to avoid costly PrinStream init
 * unless we really need it
 */
public class ConsoleOutputStream extends PrintStream {

	public ConsoleOutputStream() {
		// that's a hack - it only works because we intercept the ctor in the
		// native peer
		// otherwise it would throw an exception
		super((OutputStream) null);
	}

	@Override
	public void flush() {
		// we are not buffered anyways
	}

	@Override
	public void close() {
		// nothing to close
	}

	@Override
	public native void print(boolean b);

	public native void print(byte b);

	@Override
	public native void print(char c);

	public native void print(short s);

	@Override
	public native void print(int i);

	@Override
	public native void print(long l);

	@Override
	public native void print(float f);

	@Override
	public native void print(double d);

	@Override
	public native void print(String s);

	@Override
	public void print(Object o) {
		if (o == null) {
			print("null");
		} else {
			print(o.toString());
		}
	}

	@Override
	public native void println(boolean b);

	public native void println(byte b);

	@Override
	public native void println(char c);

	public native void println(short s);

	@Override
	public native void println(int i);

	@Override
	public native void println(long l);

	@Override
	public native void println(float f);

	@Override
	public native void println(double d);

	@Override
	public native void println(String s);

	@Override
	public void println(Object o) {
		if (o == null) {
			println("null");
		} else {
			println(o.toString());
		}
	}

	@Override
	public native void println();

	@Override
	public native PrintStream printf(String fmt, Object... args);

	@Override
	public PrintStream format(String fmt, Object... args) {
		return printf(fmt, args);
	}

	@Override
	public native void write(int b);

	@Override
	public native void write(byte[] buf, int off, int len);
}
