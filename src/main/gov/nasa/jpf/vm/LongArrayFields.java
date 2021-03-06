//
// Copyright (C) 2008 United States Government as represented by the
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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.util.HashData;
import gov.nasa.jpf.util.IntVector;

import java.io.PrintStream;

/**
 * element values for long[] objects
 */
public class LongArrayFields extends ArrayFields {

	long[] values;

	public LongArrayFields(int length) {
		values = new long[length];
	}

	@Override
	public long[] asLongArray() {
		return values;
	}

	@Override
	protected void printValue(PrintStream ps, int idx) {
		ps.print(values[idx]);
	}

	@Override
	public Object getValues() {
		return values;
	}

	@Override
	public int arrayLength() {
		return values.length;
	}

	@Override
	public int getHeapSize() { // in bytes
		return values.length * 8;
	}

	@Override
	public void appendTo(IntVector v) {
		v.appendBits(values);
	}

	@Override
	public LongArrayFields clone() {
		LongArrayFields f = (LongArrayFields) cloneFields();
		f.values = values.clone();
		return f;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LongArrayFields) {
			LongArrayFields other = (LongArrayFields) o;

			long[] v = values;
			long[] vOther = other.values;
			if (v.length != vOther.length) {
				return false;
			}

			for (int i = 0; i < v.length; i++) {
				if (v[i] != vOther[i]) {
					return false;
				}
			}

			return compareAttrs(other);

		} else {
			return false;
		}
	}

	@Override
	public void setLongValue(int pos, long newValue) {
		values[pos] = newValue;
	}

	@Override
	public long getLongValue(int pos) {
		return values[pos];
	}

	@Override
	public void hash(HashData hd) {
		long[] v = values;
		for (int i = 0; i < v.length; i++) {
			hd.add(v[i]);
		}
	}

}
