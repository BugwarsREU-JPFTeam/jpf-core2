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

import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

/**
 * Return double from method ..., value => [empty]
 */
public class DRETURN extends LongReturn {

	public double getReturnValue() {
		return Types.longToDouble(ret);
	}

	@Override
	public Object getReturnValue(ThreadInfo ti) {
		if (!isCompleted(ti)) { // we have to pull it from the operand stack
			StackFrame frame = ti.getTopFrame();
			ret = frame.peekLong();
		}

		return new Double(Types.longToDouble(ret));
	}

	@Override
	public int getByteCode() {
		return 0xAF;
	}

	@Override
	public String toString() {
		return "dreturn " + mi.getFullName();
	}

	@Override
	public void accept(InstructionVisitor insVisitor) {
		insVisitor.visit(this);
	}
}
