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
import gov.nasa.jpf.util.test.TestJPF;

import gov.nasa.jpf.jvm.*;
import gov.nasa.jpf.jvm.bytecode.*;

import org.junit.Test;


/**
 * testing various aspects of listeners on INVOKE instructions
 */
public class InvokeListenerTest extends TestJPF {

  //--- this is only used outside JPF execution
  public static class Listener extends ListenerAdapter {

    void checkArgs (JVM vm, boolean isPostExec){
      Instruction insn = vm.getLastInstruction();

      if (insn instanceof InvokeInstruction){
        InvokeInstruction call = (InvokeInstruction)insn;
        ThreadInfo ti = vm.getLastThreadInfo();
        MethodInfo mi = call.getInvokedMethod(ti);
        String mname = mi.getName();

        if (mname.equals("testInstanceMethod")){
          Object[] args = call.getArgumentValues(ti);
          ElementInfo ei = getTarget(ti,call);
          log(mname, ei, args, isPostExec);
          assert ((Double)args[0]) == 42.0;
          assert ((Integer)args[1]) == 1;

        } else if (mname.equals("testStaticMethod")){
          Object[] args = call.getArgumentValues(ti);
          ElementInfo ei = getTarget(ti,call);
          log(mname, ei, args, isPostExec);
          assert ((Integer)args[0]) == 42;

        } else if (mname.equals("testNativeInstanceMethod")){
          Object[] args = call.getArgumentValues(ti);
          ElementInfo ei = getTarget(ti,call);
          log(mname, ei, args, isPostExec);
          assert ((Double)args[0]) == 42.0;
          assert ((Integer)args[1]) == 1;

        }
      }
    }

    ElementInfo getTarget (ThreadInfo ti, InvokeInstruction call){
      if (call instanceof VirtualInvocation){
        int objRef = ((VirtualInvocation)call).getThis(ti);
        return ti.getVM().getDynamicArea().get(objRef);
      } else if (call instanceof INVOKESTATIC){
        return ((INVOKESTATIC)call).getInvokedMethod().getClassInfo().getStaticElementInfo();
      } else {
        return null;
      }
    }

    void log (String mname, ElementInfo ei, Object[] args, boolean isPostExec){
      System.out.print(isPostExec ? "# instructionExecuted: " : "# executeInstruction: ");

      System.out.print(ei);
      System.out.print('.');
      System.out.print(mname);

      System.out.print(" (");
      for (int i=0; i<args.length; i++) {
        if (i >0) System.out.print(',');
        System.out.print( args[i]);
      }
      System.out.println(")");
    }

    public void executeInstruction (JVM vm){
      checkArgs(vm, false);
    }

    public void instructionExecuted (JVM vm){
      checkArgs(vm, true);
    }

  }

  public static void main (String[] args){
    runTestsOfThisClass(args);
  }

  //--- the test methods
  

  double testInstanceMethod (double d, int c){
    return d + c;
  }
  @Test public void testInstanceMethod (){
    if (verifyNoPropertyViolation("+jpf.listener=.test.mc.basic.InvokeListenerTest$Listener")){
      testInstanceMethod(42.0, 1);
    }
  }
  
  int testStaticMethod (int a){
    return a + 1;
  }
  @Test public void testStaticMethod (){
    if (verifyNoPropertyViolation("+jpf.listener=.test.mc.basic.InvokeListenerTest$Listener")){
      testStaticMethod(42);
    }
  }

  native double testNativeInstanceMethod (double d, int c);
  @Test public void testNativeInstanceMethod (){
    if (verifyNoPropertyViolation("+jpf.listener=.test.mc.basic.InvokeListenerTest$Listener")){
      testNativeInstanceMethod(42.0, 1);
    }
  }


}