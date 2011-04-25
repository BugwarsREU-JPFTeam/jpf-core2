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
package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class ObjectStreamTest extends TestJPF {
  String osFileName = "file";
  
  @Before
  public void beforeClass() {
    File osFile = new File(osFileName);

    if (osFile.exists()) {
      osFile.delete();
    }
  }


  @Test
  public void testWriteReadInteger() {
    if (!isJPFRun()) {
      Verify.writeObjectToFile(new Integer(123), osFileName);
    }

    if (verifyNoPropertyViolation()) {
      Integer i = Verify.readObjectFromFile(Integer.class, osFileName);

      assert i == 123;
    }
  }

  @Test
  public void testWriteReadString() {
    if (!isJPFRun()) {
      Verify.writeObjectToFile(new String("hello"), osFileName);
    }

    if (verifyNoPropertyViolation()) {
      String s = Verify.readObjectFromFile(String.class, osFileName);
      assert s.equals("hello");
    }
  }
  
  static class Sup implements Serializable {
    int s;
  }
  
  static class Inherited extends Sup{
    int i;
  }
  
  @Test
  public void testWriteReadInheritedClass() {
    if (!isJPFRun()) {
      Inherited inh = new Inherited();
      inh.s = 1;
      inh.i = 2;

      Verify.writeObjectToFile(inh, osFileName);
    }

    if (verifyNoPropertyViolation(";+classpath={jpf-core}/build/tests")) {
      Inherited inh = Verify.readObjectFromFile(Inherited.class, osFileName);

      assert inh.s == 1;
      assert inh.i == 2;
    }

  }

  static class WithTransient {
    int i;
    transient int t;
  }

  @Test
  public void testWriteReadTransientField() {
    if (!isJPFRun()) {
      WithTransient wt = new WithTransient();
      wt.i = 10;
      wt.t = 10;
    }

    if (verifyNoPropertyViolation(";+classpath={jpf-core}/build/tests")) {
      WithTransient wt = Verify.readObjectFromFile(WithTransient.class, osFileName);

      assert wt.i == 10;
      // t is transient
      assert wt.t == 0;
    }
  }

  class SerializableArrayList<T> extends ArrayList<T> implements Serializable {}

  @Test
  public void testWriteReadArrayList() {
    if (!isJPFRun()) {
      ArrayList<Integer> al = new ArrayList<Integer>();
      al.add(1);
      al.add(2);
      al.add(3);
      Verify.writeObjectToFile(al, osFileName);
    }

    if (verifyNoPropertyViolation()) {
      ArrayList al = Verify.readObjectFromFile(ArrayList.class, osFileName);

      assert al.size() == 3;
      assert al.get(0).equals(1);
      assert al.get(1).equals(2);
      assert al.get(2).equals(3);
    }
  }
}
