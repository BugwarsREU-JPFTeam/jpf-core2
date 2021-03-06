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

import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Return reference from method ..., objectref => [empty]
 */
public class ARETURN extends ReturnInstruction {
	int ret;

	@Override
	public int getReturnTypeSize() {
		return 1;
	}

	@Override
	protected Object getReturnedOperandAttr(StackFrame frame) {
		return frame.getOperandAttr();
	}

	@Override
	protected void getAndSaveReturnValue(StackFrame frame) {
		ret = frame.pop();
	}

	@Override
	protected void pushReturnValue(StackFrame frame) {
		frame.pushRef(ret);
	}

	public int getReturnValue() {
		return ret;
	}

	@Override
	public Object getReturnValue(ThreadInfo ti) {
		if (!isCompleted(ti)) { // we have to pull it from the operand stack
			StackFrame frame = ti.getTopFrame();
			ret = frame.peek();
		}

		if (ret == MJIEnv.NULL) {
			return null;
		} else {
			return ti.getElementInfo(ret);
		}
	}

	@Override
	public int getByteCode() {
		return 0xB0;
	}

	@Override
	public String toString() {
		return "areturn " + mi.getFullName();
	}

	@Override
	public void accept(InstructionVisitor insVisitor) {
		insVisitor.visit(this);
	}

}
