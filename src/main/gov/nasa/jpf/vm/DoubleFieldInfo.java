//
// Copyright (C) 2006 United States Government as represented by the
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

import gov.nasa.jpf.JPFException;

/**
 * type, name and attribute information for 'double' fields
 */
public class DoubleFieldInfo extends DoubleSlotFieldInfo {
	double init;

	public DoubleFieldInfo(String name, int modifiers) {
		super(name, "D", modifiers);
	}

	@Override
	public void setConstantValue(Object constValue) {
		if (constValue instanceof Double) {
			cv = constValue;
			init = ((Double) constValue).doubleValue();

		} else {
			throw new JPFException("illegal boolean ConstValue=" + constValue);
		}
	}

	@Override
	public void initialize(ElementInfo ei, ThreadInfo ti) {
		ei.getFields().setDoubleValue(storageOffset, init);
	}

	@Override
	public Class<? extends ChoiceGenerator<?>> getChoiceGeneratorType() {
		return DoubleChoiceGenerator.class;
	}

	@Override
	public int getStorageSize() {
		return 2;
	}

	@Override
	public String valueToString(Fields f) {
		double d = f.getDoubleValue(storageOffset);
		return Double.toString(d);
	}

	@Override
	public Object getValueObject(Fields f) {
		double d = f.getDoubleValue(storageOffset);
		return new Double(d);
	}

	@Override
	public boolean isDoubleField() {
		return true;
	}

	@Override
	public boolean isNumericField() {
		return true;
	}

	@Override
	public boolean isFloatingPointField() {
		return true;
	}
}
