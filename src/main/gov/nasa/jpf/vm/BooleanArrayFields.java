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
 * element values for boolean[] objects
 */
public class BooleanArrayFields extends ArrayFields {

	boolean[] values;

	public BooleanArrayFields(int length) {
		values = new boolean[length];
	}

	@Override
	protected void printValue(PrintStream ps, int idx) {
		ps.print(values[idx] ? 't' : 'f');
	}

	@Override
	public boolean[] asBooleanArray() {
		return values;
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
	public int getHeapSize() {
		return values.length * 4;
	}

	/**
	 * we check for type and equal element values
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof BooleanArrayFields) {
			BooleanArrayFields other = (BooleanArrayFields) o;

			boolean[] v = values;
			boolean[] vOther = other.values;
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
	public BooleanArrayFields clone() {
		BooleanArrayFields f = (BooleanArrayFields) cloneFields();
		f.values = values.clone();
		return f;
	}

	// for serialization
	@Override
	public void appendTo(IntVector v) {
		v.appendPacked(values);
	}

	@Override
	public boolean getBooleanValue(int pos) {
		return values[pos];
	}

	@Override
	public void setBooleanValue(int pos, boolean v) {
		values[pos] = v;
	}

	@Override
	public void hash(HashData hd) {
		boolean[] v = values;
		for (int i = 0; i < v.length; i++) {
			hd.add(v[i]);
		}
	}

}
