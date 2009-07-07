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

package gov.nasa.jpf.tools;

import java.io.*;
import java.util.*;

import gov.nasa.jpf.*;
import gov.nasa.jpf.jvm.*;
import gov.nasa.jpf.jvm.bytecode.*;
import gov.nasa.jpf.jvm.choice.*;
import gov.nasa.jpf.report.*;
import gov.nasa.jpf.search.*;

/**
 * a listener that collects information about ChoiceGenerators, choices and
 * where they are used. The purpose is to find out what causes the state space
 * size, and to give hints of how to reduce it.
 * The interesting part is that this is a listener that doesn't work off traces,
 * but needs to collect info up to a point where we want it to report. That's
 * state space or resource related, i.e. a combination of
 *
 *  - number of transitions
 *  - memory consumption
 *  - elapsed time
 *
 * once the limit is reached, we stop the search and report.
 *
 * There are two parts we are interested in:
 *
 *  - what CGs do we have
 *  - what creates those CGs (thread,insn,source) = last step insn
 */

public class StateSpaceAnalyzer extends ListenerAdapter implements PublisherExtension
{
   // Search termination conditions
   private final long m_maxTime;
   private final long m_maxMemory;
   private final int  m_maxStates;
   private final int  m_maxChoices;

   private final ArrayList<ChoiceGenerator> m_generators = new ArrayList<ChoiceGenerator>(); // Keep ChoiceGenerators so we don't lose any in backtracking

   private final ArrayList<CGGrouper> m_groupers = new ArrayList<CGGrouper>();
   private final int m_maxOutputLines; // how many detail lines do we display in the report

   private long m_terminateTime;
   private int  m_choiceCount;

   public StateSpaceAnalyzer(Config config, JPF jpf)
   {
      m_maxStates      = config.getInt("ssa.max_states", -1);
      m_maxTime        = config.getDuration("ssa.max_time", -1);
      m_maxMemory      = config.getMemorySize("ssa.max_memory", -1);
      m_maxChoices     = config.getInt("ssa.max_choices", -1);
      m_maxOutputLines = config.getInt("ssa.max_output_lines", 10);

      initGroupers(config);

      jpf.addPublisherExtension(ConsolePublisher.class, this);
      jpf.addPublisherExtension(HTMLPublisher.class, this);
   }

   private void initGroupers(Config config)
   {
      HashMap<String, CGAccessor> accessors;
      CGGrouper grouper;
      int i;

      if (config.getStringArray("ssa.sort_order", null) == null)
      {
         config.setProperty("ssa.sort_order", "type");
         config.setProperty("ssa.sort_order2", "package,class,method,instruction,type");
      }

      accessors = new HashMap<String, CGAccessor>(5);
      accessors.put("package",     new CGPackageAccessor());
      accessors.put("class",       new CGClassAccessor());
      accessors.put("method",      new CGMethodAccessor());
      accessors.put("instruction", new CGInstructionAccessor());
      accessors.put("type",        new CGTypeAccessor());

      m_groupers.add(initGrouper(config, "ssa.sort_order", accessors));

      for (i = 2; true; i++)
      {
         grouper = initGrouper(config, "ssa.sort_order" + i, accessors);
         if (grouper == null)
            break;

         m_groupers.add(grouper);
      }
   }

   private CGGrouper initGrouper(Config config, String parameter, Map<String, CGAccessor> accessors)
   {
      CGGrouper grouper;
      CGAccessor list[];
      StringBuilder name;
      String key, sortOrder[];
      int i;

      sortOrder = config.getStringArray(parameter, null);
      if ((sortOrder == null) || (sortOrder.length <= 0))
         return(null);

      name = new StringBuilder();
      list = new CGAccessor[sortOrder.length];

      for (i = 0; i < sortOrder.length; i++)
      {
         key = sortOrder[i].toLowerCase();
         name.append(key);
         name.append(", ");

         list[i] = accessors.get(key);

         if (list[i] == null)
            config.exception("Unknown sort key: " + sortOrder[i] + ".  Found in parameter: " + parameter);
      }

      name.setLength(name.length() - 2);
      grouper = new CGGrouper(list, name.toString());

      return(grouper);
   }

   public void choiceGeneratorSet(JVM vm)
   {
      ChoiceGenerator generator;

      // NOTE: we get this from SystemState.nextSuccessor, i.e. when the CG
      // is actually used (which doesn't necessarily mean it produces a new state,
      // but it got created from a new state)

      generator      = vm.getLastChoiceGenerator();
      m_choiceCount += generator.getTotalNumberOfChoices();
      m_generators.add(generator);
   }

   public void searchStarted(Search search)
   {
      m_choiceCount = 0;
      m_generators.clear();
      m_terminateTime = m_maxTime + System.currentTimeMillis();
   }

   public void stateAdvanced(Search search)
   {
      if (shouldTerminate(search))
         search.terminate();
   }

   private boolean shouldTerminate(Search search)
   {
      if ((m_maxStates >= 0) && (search.getVM().getStateCount() >= m_maxStates))
         return(true);

      if ((m_maxTime >= 0) && (System.currentTimeMillis() >= m_terminateTime))
         return(true);

      if ((m_maxMemory >= 0) && (Runtime.getRuntime().totalMemory() >= m_maxMemory))
         return(true);

      if ((m_maxChoices >= 0) && (m_choiceCount >= m_maxChoices))
         return(true);

      return(false);
   }

   public void publishFinished(Publisher publisher)
   {
      ChoiceGenerator generators[];
      CGGrouper groupers[];

      generators = new ChoiceGenerator[m_generators.size()];
      m_generators.toArray(generators);

      groupers = new CGGrouper[m_groupers.size()];
      m_groupers.toArray(groupers);

      if (publisher instanceof ConsolePublisher)
         new PublishConsole((ConsolePublisher) publisher, generators, groupers, m_maxOutputLines).publish();
      else if (publisher instanceof HTMLPublisher)
         new PublishHtml((HTMLPublisher) publisher, generators, groupers, m_maxOutputLines).publish();
   }

   private enum CGType
   {
      FieldAccess,
      ObjectWait,
      ObjectNotify,
      SyncEnter,
      SyncExit,
      ThreadStart,
      ThreadTerminate,
      ThreadSuspend,
      ThreadResume,
      SyncCall,
      SyncReturn,
      AtomicOp,
      DataChoice
   }

   private interface CGAccessor
   {
      public Object getValue(ChoiceGenerator generator);
   }

   private static class CGPackageAccessor implements CGAccessor
   {
      public Object getValue(ChoiceGenerator generator)
      {
         ClassInfo ci;
         MethodInfo mi;
         Instruction instruction;

         instruction = generator.getInsn();
         if (instruction == null)
            return(null);

         mi = instruction.getMethodInfo();
         if (mi == null)
            return(null);

         ci = mi.getClassInfo();
         if (ci == null)
            return(null);

         return(ci.getPackageName());
      }
   }

   private static class CGClassAccessor implements CGAccessor
   {
      public Object getValue(ChoiceGenerator generator)
      {
         ClassInfo ci;
         MethodInfo mi;
         Instruction instruction;

         instruction = generator.getInsn();
         if (instruction == null)
            return(null);

         mi = instruction.getMethodInfo();
         if (mi == null)
            return(null);

         ci = mi.getClassInfo();
         if (ci == null)
            return(null);

         return(ci.getName());
      }
   }

   private static class CGMethodAccessor implements CGAccessor
   {
      public Object getValue(ChoiceGenerator generator)
      {
         MethodInfo mi;
         Instruction instruction;

         instruction = generator.getInsn();
         if (instruction == null)
            return(null);

         mi = instruction.getMethodInfo();
         if (mi == null)
            return(null);

         return(mi.getFullName());
      }
   }

   private static class CGInstructionAccessor implements CGAccessor
   {
      public Object getValue(ChoiceGenerator generator)
      {
         return(generator.getInsn());
      }
   }

   private static class CGTypeAccessor implements CGAccessor
   {
      private static final String OBJECT_CLASS_NAME = Object.class.getName();
      private static final String THREAD_CLASS_NAME = Thread.class.getName();

      public Object getValue(ChoiceGenerator generator)
      {
         if (generator instanceof ThreadChoiceGenerator)
            return(getType((ThreadChoiceGenerator) generator));

         if (generator instanceof BooleanChoiceGenerator)
            return(CGType.DataChoice);

         if (generator instanceof DoubleChoiceGenerator)
            return(CGType.DataChoice);

         if (generator instanceof IntChoiceGenerator)
            return(CGType.DataChoice);

         if (generator instanceof CustomBooleanChoiceGenerator)
            return(CGType.DataChoice);

         return(null);
      }

      private static CGType getType(ThreadChoiceGenerator generator)
      {
         Instruction instruction;

         instruction = generator.getInsn();
         if (instruction == null)
            return(null);

         if (instruction instanceof FieldInstruction)
            return(CGType.FieldAccess);

         if (instruction instanceof InvokeInstruction)
            return(getType((InvokeInstruction) instruction));

         if (instruction instanceof ReturnInstruction)
            return(getType(generator, (ReturnInstruction) instruction));

         if (instruction instanceof MONITORENTER)
            return(CGType.SyncEnter);

         if (instruction instanceof MONITOREXIT)
            return(CGType.SyncExit);

         return(null);
      }

      private static CGType getType(InvokeInstruction instruction)
      {
         MethodInfo mi;

         if (is(instruction, OBJECT_CLASS_NAME, "wait"))
            return(CGType.ObjectWait);

         if (is(instruction, OBJECT_CLASS_NAME, "notify"))
            return(CGType.ObjectNotify);

         if (is(instruction, OBJECT_CLASS_NAME, "notifyAll"))
            return(CGType.ObjectNotify);

         if (is(instruction, THREAD_CLASS_NAME, "start"))
            return(CGType.ThreadStart);

         if (is(instruction, THREAD_CLASS_NAME, "suspend"))
            return(CGType.ThreadSuspend);

         if (is(instruction, THREAD_CLASS_NAME, "resume"))
            return (CGType.ThreadResume);

         mi = instruction.getInvokedMethod();
         if (mi.getClassName().startsWith("java.util.concurrent.atomic."))
            return(CGType.AtomicOp);

         if (mi.isSynchronized())
            return(CGType.SyncCall);

         return(null);
      }

      private static boolean is(InvokeInstruction instruction, String className, String methodName)
      {
         MethodInfo mi;
         ClassInfo ci;

         mi = instruction.getInvokedMethod();
         if (!methodName.equals(mi.getName()))
            return(false);

         ci = mi.getClassInfo();

         if (!className.equals(ci.getName()))
            return(false);

         return(true);
      }

      private static CGType getType(ThreadChoiceGenerator generator, ReturnInstruction instruction)
      {
         MethodInfo mi;

         if (generator.getThreadInfo().countStackFrames() <= 1)  // The main thread has 0 frames.  Other threads have 1 frame.
            return(CGType.ThreadTerminate);

         mi = instruction.getMethodInfo();
         if (mi.isSynchronized())
            return(CGType.SyncReturn);

         return(null);
      }
   }

   private static class TreeNode
   {
   	private final HashMap<Object, TreeNode> m_childNodes;
   	private final ArrayList<Object>         m_sortedValues;
   	private final CGAccessor                m_accessors[];
      private final Object                    m_value;
   	private final int                       m_level;
      private       ChoiceGenerator           m_sample;
      private       int                       m_choiceCount;
   	private       int                       m_generatorCount;

   	TreeNode(CGAccessor accessors[], int level, Object value)
   	{
   		m_accessors = accessors;
   		m_level     = level;
         m_value     = value;

         if (level >= accessors.length)
   		{
   			m_childNodes   = null;
   			m_sortedValues = null;
   		}
   		else
   		{
  	   		m_sortedValues = new ArrayList<Object>();
   			m_childNodes   = new HashMap<Object, TreeNode>();
   		}
   	}

   	public void add(ChoiceGenerator generator)
   	{
   		TreeNode child;
   		Object value;

   		m_generatorCount++;
   		m_choiceCount += generator.getTotalNumberOfChoices();

   		if (isLeaf())
   		{
            if (m_sample == null)
               m_sample = generator;

   			return;
   		}

   		value = m_accessors[m_level].getValue(generator);
   		child = m_childNodes.get(value);
   		if (child == null)
   		{
   			child = new TreeNode(m_accessors, m_level + 1, value);
   			m_childNodes.put(value, child);
   		}

   		child.add(generator);
   	}

      public int getLevel()
      {
         return(m_level);
      }

      public Object getValue()
      {
         return(m_value);
      }

   	public int getChoiceCount()
   	{
   		return(m_choiceCount);
   	}

   	public int getGeneratorCount()
   	{
   		return(m_generatorCount);
   	}

      public ChoiceGenerator getSampleGenerator()
      {
         return(m_sample);
      }

      public boolean isLeaf()
      {
         return(m_childNodes == null);
      }

   	public void sort()
   	{
         Comparator<Object> comparator;

   		if (isLeaf())
   			return;

   		m_sortedValues.clear();
   		m_sortedValues.addAll(m_childNodes.keySet());

         comparator = new Comparator<Object>()
         {
            public int compare(Object value1, Object value2)
            {
               TreeNode node1, node2;

               node1 = m_childNodes.get(value1);
               node2 = m_childNodes.get(value2);

               return(node2.getChoiceCount() - node1.getChoiceCount());  // Sort descending
            }
         };

         Collections.sort(m_sortedValues, comparator);

   		for (TreeNode child : m_childNodes.values())
   			child.sort();
   	}

      public List<TreeNode> tour()
      {
         List<TreeNode> result;

         result = new ArrayList<TreeNode>();
         tour(result);

         return(result);
      }

      public void tour(List<TreeNode> list)
      {
         TreeNode child;
         Object value;
         int i;

         list.add(this);

         if (isLeaf())
            return;

         for (i = 0; i < m_sortedValues.size(); i++)
         {
            value = m_sortedValues.get(i);
            child = m_childNodes.get(value);
            child.tour(list);
         }
      }

      public String toString()
      {
         StringBuilder result;

         result = new StringBuilder();

         if (m_value == null)
            result.append("(null)");
         else
            result.append(m_value);

         result.append(" - L");
         result.append(m_level);
         result.append(" / C");
         result.append(m_choiceCount);
         result.append(" / G");
         result.append(m_generatorCount);
         result.append(" / N");
         result.append(m_childNodes.size());

         return(result.toString());
      }
   }

   private static class CGGrouper
   {
      private final CGAccessor m_accessors[];
      private final String     m_name;

      CGGrouper(CGAccessor accessors[], String name)
      {
         if (accessors.length <= 0)
            throw new IllegalArgumentException("accessors.length <= 0");

         if (name == null)
            throw new NullPointerException("name == null");

         m_accessors = accessors;
         m_name      = name;
      }

      public String getName()
      {
         return(m_name);
      }

      public int getLevelCount()
      {
         return(m_accessors.length);
      }

      public TreeNode makeTree(ChoiceGenerator generators[])
      {
			TreeNode root;
         int i;

			root = new TreeNode(m_accessors, 0, "All");

			for (i = 0; i < generators.length; i++)   // Go up so that the order is preserved in the tree.
				root.add(generators[i]);

         root.sort();

			return(root);
      }
   }

   private static abstract class Publish
   {
      protected final Publisher       m_publisher;
      protected final ChoiceGenerator m_generators[];
      protected final CGGrouper       m_groupers[];
      protected final int             m_maxOutputLines;
      protected       PrintWriter     m_output;

      Publish(Publisher publisher, ChoiceGenerator generators[], CGGrouper groupers[], int maxOutputLines)
      {
         m_publisher      = publisher;
         m_generators     = generators;
         m_groupers       = groupers;
         m_maxOutputLines = maxOutputLines;
      }

      public abstract void publish();
   }

   private static class PublishConsole extends Publish
   {
      PublishConsole(ConsolePublisher publisher, ChoiceGenerator[] generators, CGGrouper[] groupers, int maxOutputLines)
      {
         super(publisher, generators, groupers, maxOutputLines);
         m_output = publisher.getOut();
      }

      public void publish()
      {
         int i;

         for (i = 0; i < m_groupers.length; i++)
            publishSortedData(m_groupers[i]);
      }

      private void publishSortedData(CGGrouper grouper)
      {
         List<TreeNode> tour;
         TreeNode node;
         int i, lines, levelCount;

         lines      = 0;
         levelCount = grouper.getLevelCount();
         node       = grouper.makeTree(m_generators);
         tour       = node.tour();

         m_publisher.publishTopicStart("Grouped By: " + grouper.getName());

         for (i = 0; (i < tour.size()) && (lines < m_maxOutputLines); i++)
         {
            node = tour.get(i);

            publishTreeNode(node);

            if (node.isLeaf())
            {
               publishDetails(node, levelCount + 1);
               lines++;
            }
         }

         if (lines >= m_maxOutputLines)
            m_output.println("...");
      }

      private void publishTreeNode(TreeNode node)
      {
         Object value;

         // Tree
         publishPadding(node.getLevel());

         value = node.getValue();
         if (value == null)
            m_output.print("(null)");
         else
            m_output.print(value);

         // Choices
         m_output.print("  (C");
         m_output.print(node.getChoiceCount());

         // Generators
         m_output.print("  G");
         m_output.print(node.getGeneratorCount());

         m_output.println(')');
      }

      private void publishDetails(TreeNode node, int levelCount)
      {
         ChoiceGenerator generator;
         Instruction     instruction;

         generator   = node.getSampleGenerator();
         instruction = generator.getInsn();

         // Location
         publishPadding(levelCount);
         m_output.print("Location:  ");
         m_output.println(instruction.getFileLocation());

         // Code
         publishPadding(levelCount);
         m_output.print("Code:  ");
         m_output.println(instruction.getSourceLine().trim());

         // Instruction
         publishPadding(levelCount);
         m_output.print("Instruction:  ");
         m_output.println(instruction.getMnemonic());

         // Position
         publishPadding(levelCount);
         m_output.print("Position:  ");
         m_output.println(instruction.getPosition());

         // Generator Class
         publishPadding(levelCount);
         m_output.print("Generator Class:  ");
         m_output.println(generator.getClass().getName());
      }

      private void publishPadding(int levelCount)
      {
         int i;

         for (i = levelCount; i > 0; i--)
            m_output.print("   ");
      }
   }

   private static class PublishHtml extends Publish
   {
      PublishHtml(HTMLPublisher publisher, ChoiceGenerator[] generators, CGGrouper[] groupers, int maxOutputLines)
      {
         super(publisher, generators, groupers, maxOutputLines);
         m_output = publisher.getOut("State Space");
      }

      public void publish()
      {
         int i;

         m_output.println("      <style type=\"text/css\">");
         m_output.println("         table             { border-collapse: collapse; white-space: nowrap; border: 1px solid #000000; }");
         m_output.println("         th                { padding: 5px 5px; border: 1px solid #000000; background-color: #0080FF; }");
         m_output.println("         td                { padding: 0px 5px; border: none; }");
         m_output.println("         tr.treeNodeOpened { font-weight: bold; background-color: #A0D0FF; }");
         m_output.println("         tr.treeNodeClosed { font-weight: bold; background-color: #A0D0FF; }");
         m_output.println("      </style>");

         ((HTMLPublisher) m_publisher).writeTableTreeScript(m_output, 0);

         m_output.println("      <div style=\"white-space: nowrap;\">");

         for (i = 0; i < m_groupers.length; i++)
            publishSortedData(i, m_groupers[i]);

         m_output.println("      </div>");

         m_output.flush();
         m_output.close();
      }

      private void publishSortedData(int treeID, CGGrouper grouper)
      {
         HTMLPublisher publisher;
         TreeNode node;
         List<TreeNode> tour;
         List<String> nodeIDs;
         StringBuilder nodeID;
         int i, lines, lastLevel;

         lines     = 0;
         lastLevel = -1;
         publisher = (HTMLPublisher) m_publisher;
         nodeID    = new StringBuilder(Integer.toString(treeID));
         node      = grouper.makeTree(m_generators);
         tour      = node.tour();
         nodeIDs   = new ArrayList<String>();

         m_output.println("<hr/>");

         m_output.print("         <h2>Grouped By: ");
         m_output.print(HTMLPublisher.escape(grouper.getName()));
         m_output.println("</h2>");

         publisher.writeTableTreeBegin(m_output);

         // Write header row
         m_output.println("         <thead>");
         m_output.println("            <tr>");

         m_output.print("               <th>");
         m_output.print("");
         m_output.println("</th>");

         m_output.println("               <th>Choices</th>");
         m_output.println("               <th>Generators</th>");
         m_output.println("               <th>File</th>");
         m_output.println("               <th>Line</th>");
         m_output.println("               <th>Instruction</th>");
         m_output.println("               <th>Position</th>");
         m_output.println("               <th>Code</th>");
         m_output.println("               <th>Generator Class</th>");
         m_output.println("            </tr>");
         m_output.println("         </thead>");
         m_output.println("         <tbody>");

         for (i = 0; (i < tour.size()) && (lines < m_maxOutputLines); i++)
         {
            node = tour.get(i);

            updateNodeID(nodeID, node.getLevel(), lastLevel, i);
            lastLevel = node.getLevel();

            publisher.writeTableTreeNodeBegin(m_output, nodeID.toString());
            publishTreeNode(node);

            if (node.isLeaf())
            {
               publishDetails(node);
               nodeIDs.add(nodeID.toString());
               lines++;
            }

            publisher.writeTableTreeNodeEnd(m_output);
         }

         if (lines >= m_maxOutputLines)
         {
            publisher.writeTableTreeNodeBegin(m_output, "...");

            m_output.print("<td>...</td>");
            m_output.print("<td></td>"); // Choices
            m_output.print("<td></td>"); // Generators
            m_output.print("<td></td>"); // File
            m_output.print("<td></td>"); // Line
            m_output.print("<td></td>"); // Instruction
            m_output.print("<td></td>"); // Position
            m_output.print("<td></td>"); // Code
            m_output.print("<td></td>"); // Generator Class

            publisher.writeTableTreeNodeEnd(m_output);
         }

         m_output.println("         </tbody>");
         publisher.writeTableTreeEnd(m_output);

         publisher.writeTableTreeOpenNodes(m_output, nodeIDs);
      }

      private static void updateNodeID(StringBuilder nodeID, int level, int lastLevel, int ID)
      {
         int pos;

         for (; lastLevel >= level; lastLevel--)
         {
            pos = nodeID.lastIndexOf("-");
            nodeID.setLength(pos);
         }

         nodeID.append('-');
         nodeID.append(ID);
      }

      private void publishTreeNode(TreeNode node)
      {
         Object value;

         // Tree
         m_output.print("<td>");

         value = node.getValue();
         if (value == null)
            m_output.print("<i>null</i>");
         else
            m_output.print(HTMLPublisher.escape(value.toString()));

         m_output.print("</td>");

         // Choices
         m_output.print("<td align=\"right\">");
         m_output.print(node.getChoiceCount());
         m_output.print("</td>");

         // Generators
         m_output.print("<td align=\"right\">");
         m_output.print(node.getGeneratorCount());
         m_output.print("</td>");

         if (!node.isLeaf())
         {
            m_output.print("<td></td>"); // File
            m_output.print("<td></td>"); // Line
            m_output.print("<td></td>"); // Instruction
            m_output.print("<td></td>"); // Position
            m_output.print("<td></td>"); // Code
            m_output.print("<td></td>"); // Generator Class
         }
      }

      private void publishDetails(TreeNode node)
      {
         ChoiceGenerator generator;
         ClassInfo ci;
         MethodInfo mi;
         Instruction instruction;
         String fileName;
         int line;

         generator   = node.getSampleGenerator();
         instruction = generator.getInsn();
         mi          = instruction.getMethodInfo();
         ci          = mi.getClassInfo();

         // File
         if (ci == null)
            fileName = "[synthetic]";
         else
            fileName = ci.getSourceFileName();

         m_output.print("<td>");
         m_output.print(HTMLPublisher.escape(fileName));
         m_output.print("</td>");

         // Line
         m_output.print("<td align=\"right\">");
         line = mi.getLineNumber(instruction);
         m_output.print(line > 0 ? line : "");

         // Instruction
         m_output.print("<td>");
         m_output.print(HTMLPublisher.escape(instruction.getMnemonic()));
         m_output.print("</td>");

         // Position
         m_output.print("<td align=\"\">");
         m_output.print(instruction.getPosition());
         m_output.print("</td>");

         // Code
         m_output.print("<td>");
         m_output.print(HTMLPublisher.escape(instruction.getSourceLine().trim()));
         m_output.print("</td>");

         // Generator Class
         m_output.print("<td>");
         m_output.print(HTMLPublisher.escape(generator.getClass().getName()));
         m_output.print("</td>");
      }
   }
}