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
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Store into reference array ..., arrayref, index, value => ...
 */
public class AASTORE extends ArrayStoreInstruction {

	int value;

	@Override
	protected void popValue(StackFrame frame) {
		value = frame.pop();
	}

	@Override
	protected void setField(ElementInfo ei, int index)
			throws ArrayIndexOutOfBoundsExecutiveException {
		ei.checkArrayBounds(index);
		ei.setReferenceElement(index, value);
	}

	@Override
	protected Instruction checkArrayStoreException(ThreadInfo ti, ElementInfo ei) {
		ClassInfo c = ei.getClassInfo();

		if (value != MJIEnv.NULL) { // no checks for storing 'null'
			ClassInfo elementCi = ti.getClassInfo(value);
			ClassInfo arrayElementCi = c.getComponentClassInfo();
			if (!elementCi.isInstanceOf(arrayElementCi)) {
				String exception = "java.lang.ArrayStoreException";
				String exceptionDescription = elementCi.getName();
				return ti.createAndThrowException(exception,
						exceptionDescription);
			}
		}

		return null;
	}

	@Override
	public int getByteCode() {
		return 0x53;
	}

	@Override
	public void accept(InstructionVisitor insVisitor) {
		insVisitor.visit(this);
	}
}
