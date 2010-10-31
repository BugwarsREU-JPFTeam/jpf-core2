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
package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.jvm.*;
import gov.nasa.jpf.util.test.*;
import java.util.*;
import org.junit.*;

/*
 * JVM.registerStartupClass must be kept in sync with ClassInfo.registerClass.
 * This test ensures that the interfaces of the main class are registered 
 * properly.  The old JVM.registerStartupClass code wasn't initializing the
 * class object of the interfaces.
 */
public class InitializeInterfaceClassObjectRefTest extends TestJPF implements InitializeInterfaceClassObjectRefTestInterface
{
   public static void main(String args[])
   {
      runTestsOfThisClass(args);   // Not really needed since test() doesn't do anything when running in JPF
   }
   
   @Test
   public void test()
   {
      if (verifyNoPropertyViolation("+log.finest+=,gov.nasa.jpf.jvm.ClassInfo"))
      {
         // nothing to do while running in JPF
      }
      else
      {
         ClassInfo ci = ClassInfo.getResolvedClassInfo(InitializeInterfaceClassObjectRefTestInterface.class.getName());
         
         if (ci.getClassObjectRef() < 0)
            throw new AssertionError();
      }
   }
}

interface InitializeInterfaceClassObjectRefTestInterface
{
}
