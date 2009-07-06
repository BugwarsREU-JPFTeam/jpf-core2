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
package gov.nasa.jpf.test.mc.basic;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.*;
import gov.nasa.jpf.jvm.bytecode.*;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * raw test for field/operand/local attribute handling
 */
public class AttrsTest extends TestJPF {

//------------ this part we only need outside the JPF execution
  static final String ATTR = "\"the answer to the ultimate question of life, the universe, and everything\"";

  public static class IntListener extends ListenerAdapter {

    public IntListener () {}

    public void instructionExecuted (JVM vm){
      Instruction insn = vm.getLastInstruction();
      MethodInfo mi = insn.getMethodInfo();

      // not very efficient, but who cares - it's a small test
      if (insn instanceof ISTORE){
        if (mi.getName().equals("testIntPropagation")){
          ISTORE istore = (ISTORE)insn;
          ThreadInfo ti = vm.getLastThreadInfo();
          String localName = istore.getLocalVariableName();
          int localIndex = istore.getLocalVariableIndex();

          if (localName.equals("i")){
            ti.setLocalAttr(localIndex, ATTR);
            Object a = ti.getLocalAttr(localIndex);
            System.out.println("'i' attribute set to: " + a);

          } else if (localName.equals("j")){
            Object a = ti.getLocalAttr(localIndex);
            System.out.println("'j' attribute: " + a);

            /** get's overwritten in the model class
            if (a != ATTR){
              throw new JPFException("attribute propagation failed");
            }
            **/
          }
        }
      }
    }
  }

  public static class DoubleListener extends ListenerAdapter {

    public DoubleListener () {}

    public void instructionExecuted (JVM vm){
      Instruction insn = vm.getLastInstruction();
      MethodInfo mi = insn.getMethodInfo();

      if (insn instanceof DSTORE){
        if (mi.getName().equals("testDoublePropagation")){
          DSTORE dstore = (DSTORE)insn;
          ThreadInfo ti = vm.getLastThreadInfo();
          String localName = dstore.getLocalVariableName();
          int localIndex = dstore.getLocalVariableIndex();

          if (localName.equals("d")){
            ti.setLocalAttr(localIndex, ATTR);

          } else if (localName.equals("r")){
            Object a = ti.getLocalAttr(localIndex);
            System.out.println("'r' attribute: " + a);
            /** get's overwritten in the model class
            if (a != ATTR){
              throw new JPFException("attribute propagation failed");
            }
            **/
          }
        }

      }
    }
  }

  public static class InvokeListener extends ListenerAdapter {

    public void instructionExecuted (JVM vm){
      Instruction insn = vm.getLastInstruction();
      if (insn instanceof InvokeInstruction) {
        InvokeInstruction call = (InvokeInstruction)insn;
        ThreadInfo ti = vm.getLastThreadInfo();
        MethodInfo mi = call.getInvokedMethod();
        String mName = mi.getName();
        if (mName.equals("goModel") || mName.equals("goNative")) {
          Object[] a = call.getArgumentAttrs(ti);
          assert a != null & a.length == 3;

          System.out.println("listener notified of: " + mName + "(), attributes= "
                             + a[0] + ',' + a[1] + ',' + a[2]);

          assert a[0] instanceof Integer && a[1] instanceof Integer;
          assert (((Integer)a[0]).intValue() == 1) &&
                 (((Integer)a[1]).intValue() == 2) &&
                 (((Integer)a[2]).intValue() == 3);
        }
      }
    }
  }


  public static void main (String[] args){
    runTestsOfThisClass( args);
  }

//------------



  static int sInt;
  int iInt;

  static double sDouble;
  double iDouble;

  int echoInt (int a){
    return a;
  }

  @Test public void testIntPropagation () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.AttrsTest$IntListener")) {
      int i = 42; // this gets attributed
      Verify.setLocalAttribute("i", 42); // this overwrites whatever the ISTORE listener did set on 'i'

      iInt = echoInt(i);
      sInt = iInt;
      int j = sInt; // now j should have the initial i attribute, and value 42

      int attr = Verify.getLocalAttribute("j");
      Verify.print("@ 'j' attribute after assignment: " + attr);
      Verify.println();

      assert attr == 42;
    }
  }

  @Test public void testDoublePropagation () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.AttrsTest$DoubleListener")) {
      double d = 42.0; // this gets attributed
      Verify.setLocalAttribute("d", 42);  // this overwrites whatever the ISTORE listener did set on 'd'

      iDouble = echoDouble(d);
      sDouble = iDouble;

      //double r = sDouble; // now r should have the same attribute
      double r = echoDouble(d);

      int attr = Verify.getLocalAttribute("r");
      Verify.print("@ 'r' attribute after assignment: " + attr);
      Verify.println();

      assert attr == 42;
    }
  }

  @Test public void testInvokeListener () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.AttrsTest$InvokeListener")) {
      Verify.setLocalAttribute("this", 1);

      double d = 42.0;
      Verify.setLocalAttribute("d", 2);

      int i = 42;
      Verify.setLocalAttribute("i", 3);

      double result = goNative(d, i); // that's going to be listened on
      int attr = Verify.getLocalAttribute("result");

      Verify.print("@ 'result' attribute: " + attr);
      Verify.println();

      assert attr == 6;

      int r = goModel(d, i);  // that's listened for, too
      assert r == 6;
    }
  }


  native double goNative (double d, int i);

  @Test public void testNativeMethod () {
    if (verifyNoPropertyViolation()) {
      Verify.setLocalAttribute("this", 1);

      double d = 42.0;
      Verify.setLocalAttribute("d", 2);

      int i = 42;
      Verify.setLocalAttribute("i", 3);

      double result = goNative(d, i);
      int attr = Verify.getLocalAttribute("result");

      Verify.print("@ 'result' attribute: " + attr);
      Verify.println();

      assert attr == 6;
    }
  }


  int goModel (double d, int i) {
    int a1 = Verify.getLocalAttribute("d");
    int a2 = Verify.getLocalAttribute("i");

    return a1*a2;
  }

  double echoDouble (double d){
    return d;
  }


  @Test public void testExplicitRef () {
    if (verifyNoPropertyViolation()) {
      int attr = Verify.getFieldAttribute(this, "iDouble");
      Verify.print("@ 'iDouble' attribute before set: ", Integer.toString(attr));
      Verify.println();

      Verify.setFieldAttribute(this, "iDouble", 42);

      attr = Verify.getFieldAttribute(this, "iDouble");
      Verify.print("@ 'iDouble' attribute after set: ", Integer.toString(attr));
      Verify.println();

      assert attr == 42;
    }
  }

  public void testExplicitArrayRef () {
    if (verifyNoPropertyViolation()) {
      int attr;
      double[] myArray = new double[10];

      attr = Verify.getElementAttribute(myArray, 5);
      Verify.print("@ 'myArray[5]' attribute before set: ", Integer.toString(attr));
      Verify.println();

      Verify.setElementAttribute(myArray, 5, 42);

      attr = Verify.getElementAttribute(myArray, 5);
      Verify.print("@ 'myArray[5]' attribute after set: ", Integer.toString(attr));
      Verify.println();

      assert attr == 42;
    }
  }

  public void testArraycopy () {
    if (verifyNoPropertyViolation()) {
      int attr;
      double[] a1 = new double[10];
      double[] a2 = new double[10];

      Verify.setElementAttribute(a1, 3, 42);
      System.arraycopy(a1, 1, a2, 0, 3);

      attr = Verify.getElementAttribute(a2, 2);
      assert attr == 42;
    }
  }

  double ddd;

  public void testArrayPropagation() {
    if (verifyNoPropertyViolation()) {

      int attr;
      double[] a1 = new double[10];
      double[] a2 = new double[10];

      Verify.setElementAttribute(a1, 3, 42);

      //attr = Verify.getElementAttribute(a1,3);
      //System.out.println(attr);

      ddd = a1[3];
      //Verify.setFieldAttribute(this,"ddd",42);
      //attr = Verify.getFieldAttribute(this,"ddd");
      //System.out.println("@ ddd : " + attr);

      double d = ddd;
      //ccc = d;
      //attr = Verify.getFieldAttribute(this,"ccc");
      //System.out.println("ccc ; " + attr);

      //double d = a1[3]; // now d should have the attr
      a2[0] = d;
      attr = Verify.getElementAttribute(a2, 0);
      System.out.println("@ a2[0] : " + attr);

      assert attr == 42;
    }
  }

  public void testBacktrack() {
    if (verifyNoPropertyViolation()) {
      int v = 42; // need to init or the compiler does not add it to the name table
      Verify.setLocalAttribute("v", 42);

      boolean b = Verify.getBoolean(); // restore point
      System.out.println(b);

      int attr = Verify.getLocalAttribute("v");
      System.out.println(attr);

      Verify.setLocalAttribute("v", -1);
      attr = Verify.getLocalAttribute("v");
      System.out.println(attr);
    }
  }
  
  public void testInteger() {
    if (verifyNoPropertyViolation()) {
      int v = 42;
      Verify.setLocalAttribute("v", 4200);

      // explicit
      Integer o = new Integer(v);
      int j = o.intValue();
      int attr = Verify.getLocalAttribute("j");
      assert attr == 4200;

      // semi autoboxed
      j = o;
      boolean b = Verify.getBoolean(); // just cause some backtracking damage
      attr = Verify.getLocalAttribute("j");
      assert attr == 4200;

    /** this does not work because of cached, preallocated Integer objects)
    // fully autoboxed
    Integer a = v;
    j = a;
    attr = Verify.getLocalAttribute("j");
    assert attr == 4200;
     **/
    }
  }
}
