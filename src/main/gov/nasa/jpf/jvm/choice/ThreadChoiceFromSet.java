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
package gov.nasa.jpf.jvm.choice;

import gov.nasa.jpf.jvm.ThreadChoiceGenerator;
import gov.nasa.jpf.jvm.ThreadInfo;

import java.io.PrintWriter;

public class ThreadChoiceFromSet extends ThreadChoiceGenerator {

  protected ThreadInfo[] values;
  protected int count;
  
  public ThreadChoiceFromSet (ThreadInfo[] set, boolean isSchedulingPoint) {
    super(isSchedulingPoint);
    
    values = set;
    count = -1;
  }
  
  public void reset () {
    count = -1;
  }
  
  public ThreadInfo getNextChoice () {
    if ((count >= 0) && (count < values.length)) {
      return values[count];
    } else {
      // we don't raise an exception here because this might be (mis)used
      // from a listener, which shouldn't produce JPFExceptions
      return null;
    }
  }

  public boolean hasMoreChoices () {
    return !isDone && (count < values.length-1);
  }

  /**
   * this has to handle timeouts, which we do with temporary thread status
   * changes (i.e. the TIMEOUT_WAITING threads are in our list of choices, but
   * only change their status to TIMEDOUT when they are picked as the next choice)
   */
  public void advance () {
    
    if (count >= 0) {
      if (values[count].isTimedOut()) {
        // we've temporarily set this to TIMEDOUT in the previous choice, now
        // set it to TIMEOUT_WAITING again before we run the next choice
        values[count].resetTimedOut();
      }
    }
    
    if (count < values.length-1) {
      count++;
      
      if (values[count].isTimeoutWaiting()) {
        // first time we see a TIMEOUT_WAITING thread, we change it's status
        // to TIMEDOUT and run it
        values[count].setTimedOut();
      }
    }
  }

  public int getTotalNumberOfChoices () {
    return values.length;
  }

  public int getProcessedNumberOfChoices () {
    return count+1;
  }

  public Object getNextChoiceObject () {
    return getNextChoice();
  }
  
  public void printOn (PrintWriter pw) {
    pw.print(getClass().getName());
    pw.print(" {");
    for (int i=0; i<values.length; i++) {
      if (i > 0) pw.print(',');
      if (i == count) {
        pw.print(MARKER);
      }
      pw.print(values[i].getName());
    }
    pw.print("}");
  }
  
  public ThreadChoiceFromSet randomize () {
    for (int i = values.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      ThreadInfo tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
    }
    return this;
  }
  
  public ThreadInfo[] getAllThreadChoices() {
	  return values; 
  }
  
}