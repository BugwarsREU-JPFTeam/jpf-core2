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

import gov.nasa.jpf.jvm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.jvm.BooleanArrayFields;
import gov.nasa.jpf.jvm.ByteArrayFields;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.Fields;
import gov.nasa.jpf.jvm.ThreadInfo;

/**
 * Store into byte or boolean array
 * ..., arrayref, index, value  => ...
 */
public class BASTORE extends ArrayStoreInstruction {

  byte value;

  protected void popValue(ThreadInfo ti){
    value = (byte)ti.pop();
  }

  protected void setField (ElementInfo ei, int index) throws ArrayIndexOutOfBoundsExecutiveException {
    ei.checkArrayBounds(index);

    Fields f = ei.getFields();

    if (f instanceof ByteArrayFields){
      ei.setByteElement(index, value);

    } else if (f instanceof BooleanArrayFields){
      ei.setBooleanElement(index, value != 0 ? true : false);
    }

  }

  public int getByteCode () {
    return 0x54;
  }
  
  public void accept(InstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
