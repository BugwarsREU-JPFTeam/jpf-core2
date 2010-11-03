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
package gov.nasa.jpf.listener;

import java.util.*;
import gov.nasa.jpf.*;
import gov.nasa.jpf.jvm.*;
import gov.nasa.jpf.search.*;
import gov.nasa.jpf.search.heuristic.*;
import java.util.logging.*;

/**
 * A listener that tracks information about the stack depth of when a lock is acquired.
 *
 * Writing a test for this class is very difficult.  Hence, a lot of asserts are added.
 */
public class LockedStackDepth extends ListenerAdapter
{
   private static final Logger  s_logger    = JPF.getLogger(LockedStackDepth.class.getName());
   private static final Integer EMPTY[]     = new Integer[0];
   private static final int     THREAD_FLAG = 0x80000000;
   
   private final HashMap<Integer, Operation> m_operations = new HashMap<Integer, Operation>();
   private final HashMap<Integer, Integer>   m_state      = new HashMap<Integer, Integer>();
   private final HashMap<Operation, Integer> m_index      = new HashMap<Operation, Integer>();
   private final ArrayList<Operation>        m_apply      = new ArrayList<Operation>();
   
   private Operation m_current;

   public int getLockedStackDepth(ElementInfo lock)
   {
      Integer result;
      int lockIndex;
      
      lockIndex = lock.getIndex();
      result    = m_state.get(lockIndex);
      
      if (s_logger.isLoggable(Level.INFO))
         s_logger.info("Depth = " + result + " | Lock Index = " + lockIndex + " | Lock = " + lock);
      
      if (result == null)
         return(-1);
      
      assert result >= 0;
      
      return(result);
   }

   public void objectLocked(JVM vm)
   {
      ThreadInfo thread;
      ElementInfo lock;
      Integer depth;

      lock   = vm.getLastElementInfo();
      thread = vm.getLastThreadInfo();
      depth  = new Operation(thread, null).getOldDepth();

      if (depth == null)
         depth = thread.getStackDepth();

      assert thread.getLockCount() == 0;
      assert thread.getLockObject() == null;
      assert lock.isLockedBy(thread);
      
      if (m_state.containsKey(lock.getIndex()))
         assert !m_state.containsKey(lock.getIndex());
            
      assert !m_state.containsKey(thread.getThreadObjectRef());
      assert depth >= 0;

      new Operation(lock, depth);
   }

   public void objectUnlocked(JVM vm)
   {
      ThreadInfo thread;
      ElementInfo lock;
      Integer depth;

      thread = vm.getLastThreadInfo();
      lock   = vm.getLastElementInfo();
      depth  = new Operation(lock, null).getOldDepth();

      assert !m_state.containsKey(lock.getIndex());
      assert !m_state.containsKey(thread.getThreadObjectRef());
      assert depth >= 0;

      if (thread.isWaiting())
      {
         assert !lock.isLockedBy(thread);
         assert lock.getLockCount() == 0;
         assert thread.getLockCount() > 0;
         assert thread.getLockObject() == lock;
         new Operation(thread, depth);
      }
      else
      {
         assert lock.isLockedBy(thread);
         assert lock.getLockCount() > 0;
         assert thread.getLockCount() == 0;
         assert thread.getLockObject() == null;
      }
   }

   public void searchStarted(Search search)
   {
      m_operations.clear();
      
      m_current = null;
   }

   public void stateAdvanced(Search search)
   {
      Integer id;
      
      id = search.getStateId();
      
      if (!m_operations.containsKey(id))       // Don't overwrite the original chain of Operations to get to the same state.  The original chain is more likely to be shorter.
         m_operations.put(id, m_current);

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Advanced: " + id);
      
      logState();
   }

   public void stateProcessed(Search search)
   {
      Integer id;

      if (!(search instanceof DFSearch))  // Can't remove from m_operations since Search could go back to the state.
         if (!(search instanceof BFSHeuristic))
            return;

      id = search.getStateId();

      m_operations.remove(id);            // DFSearch won't ever revisit this state.  It is safe to remove and allow for cleanup.

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Processed: " + id);
   }

   public void stateBacktracked(Search search)
   {
      switchTo(search);
   }

   public void stateRestored(Search search)
   {
      switchTo(search);
   }
   
   private void switchTo(Search search)
   {
      Operation next;
      Integer id;
      
      id   = search.getStateId();
      next = m_operations.get(id);

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Switching: " + id);

      assert (id <= 0) || (m_operations.containsKey(id));
      
      switchTo(next);
      
      m_current = next;

      logState();

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Switched:  " + id);
   }
   
   private void switchTo(Operation next)
   {
      Operation operation;
      Integer index;
      int i;
      
      for (operation = next; operation != null; operation = operation.getParent())  // Go through all of the operations leading back to the root.
      {
         m_index.put(operation, m_apply.size());  // Keep track of the index into m_apply where operation is found
         m_apply.add(operation);
      }
      
      index = null;
      
      for (operation = m_current; operation != null; operation = operation.getParent())  // Go through all of the operations leading back to the root.
      {
         index = m_index.get(operation);
         
         if (index != null)        // If a common ancestor is found, stop going back.
            break;
         
         operation.revert();       // Revert the operation since it isn't common to both states.
      }
      
      if (index == null)
         index = m_apply.size();   // No common ancestor found.  Must apply all of the operations.
      
      for (i = index; --i >= 0; )  // Apply all of the operations required to get back to the "next" state.
         m_apply.get(i).apply();

      m_index.clear();
      m_apply.clear();
   }
   
   private void logState()
   {
      StringBuilder message;
      String type;
      Integer key, keys[], depth;
      int i;
      
      if (!s_logger.isLoggable(Level.FINEST))
         return;

      message = new StringBuilder();
      keys    = m_state.keySet().toArray(EMPTY);
      
      Arrays.sort(keys);
      message.append("State | Size = ");
      message.append(keys.length);
      
      for (i = 0; i < keys.length; i++)
      {
         key   = keys[i];
         depth = m_state.get(key);

         if ((key & THREAD_FLAG) != 0)
            type = "Thread";
         else
            type = "Lock";

         message.append('\n');
         message.append("Depth = ");
         message.append(depth);
         message.append(" | Key = ");
         message.append(key & ~THREAD_FLAG);
         message.append(" | ");
         message.append(type);
      }
      
      s_logger.finest(message.toString());
   }
   
   private class Operation
   {
      private final Operation m_parent;
      private final Integer   m_key;
      private final Integer   m_oldDepth;
      private final Integer   m_newDepth;
      
      public Operation(ElementInfo lock, Integer newDepth)
      {
         this(lock.getIndex(), newDepth);
      }
      
      public Operation(ThreadInfo thread, Integer newDepth)
      {
         this(thread.getThreadObjectRef() ^ THREAD_FLAG, newDepth);
      }
      
      private Operation(Integer key, Integer newDepth)
      {
         m_parent   = m_current;
         m_current  = this;
         m_key      = key;
         m_newDepth = newDepth;
         m_oldDepth = m_state.get(key);
         
         apply();
      }
      
      public Operation getParent()
      {
         return(m_parent);
      }
      
      public Integer getOldDepth()
      {
         return(m_oldDepth);
      }
      
      public Integer getNewDepth()
      {
         return(m_newDepth);
      }
      
      public void apply()
      {
         change(m_newDepth);
         log("Apply ");
      }
      
      public void revert()
      {
         change(m_oldDepth);
         log("Revert");
      }
      
      private void change(Integer depth)
      {
         Integer previous;
         
         if (depth == null)
            m_state.remove(m_key);
         else
         {
            previous = m_state.put(m_key, depth);
            
            assert previous == null;
         }
      }
      
      private void log(String header)
      {
         String message, subheader, depthStr, type;
         Integer depth;
         
         if (!s_logger.isLoggable(Level.FINER))
            return;

         if (m_newDepth != null)
         {
            subheader = "Add   ";
            depth     = m_newDepth;
         }
         else
         {
            subheader = "Remove";
            depth     = m_oldDepth;
         }

         depthStr = String.valueOf(depth);
         
         switch (depthStr.length())
         {
            case 1: depthStr = "   " + depthStr; break;
            case 2: depthStr = "  " + depthStr; break;
            case 3: depthStr = " " + depthStr; break;
            default: break;
         }
         
         if ((m_key & THREAD_FLAG) != 0)
            type = "Thread";
         else
            type = "Lock";
         
         message = header + " " + subheader + " | Depth = " + depthStr + " | Key = " + (m_key & ~THREAD_FLAG) + " | " + type;
            
         s_logger.finer(message);
      }
   }
}
