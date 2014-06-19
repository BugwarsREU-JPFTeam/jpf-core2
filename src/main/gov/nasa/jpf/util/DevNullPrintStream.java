//
// Copyright (C) 2014 United States Government as represented by the
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

package gov.nasa.jpf.util;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * a PrintStream that doesn't print anything
 */
public class DevNullPrintStream extends PrintStream {

	public DevNullPrintStream() {
		super(new ByteArrayOutputStream());
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}

	@Override
	public boolean checkError() {
		return false;
	}

	@Override
	protected void setError() {
	}

	@Override
	protected void clearError() {
	}

	@Override
	public void write(int a) {
	}

	@Override
	public void write(byte[] a, int b, int c) {
	}

	@Override
	public void print(boolean a) {
	}

	@Override
	public void print(char a) {
	}

	@Override
	public void print(int a) {
	}

	@Override
	public void print(long a) {
	}

	@Override
	public void print(float a) {
	}

	@Override
	public void print(double a) {
	}

	@Override
	public void print(char[] a) {
	}

	@Override
	public void print(String a) {
	}

	@Override
	public void print(Object a) {
	}

	@Override
	public void println() {
	}

	@Override
	public void println(boolean a) {
	}

	@Override
	public void println(char a) {
	}

	@Override
	public void println(int a) {
	}

	@Override
	public void println(long a) {
	}

	@Override
	public void println(float a) {
	}

	@Override
	public void println(double a) {
	}

	@Override
	public void println(char[] a) {
	}

	@Override
	public void println(String a) {
	}

	@Override
	public void println(Object a) {
	}

	@Override
	public PrintStream printf(String a, Object... b) {
		return this;
	}

	@Override
	public PrintStream printf(Locale a, String b, Object... c) {
		return this;
	}

	@Override
	public PrintStream format(String a, Object... b) {
		return this;
	}

	@Override
	public PrintStream format(Locale a, String b, Object... c) {
		return this;
	}

	@Override
	public PrintStream append(CharSequence a) {
		return this;
	}

	@Override
	public PrintStream append(CharSequence a, int b, int c) {
		return this;
	}

	@Override
	public PrintStream append(char a) {
		return this;
	}

}
