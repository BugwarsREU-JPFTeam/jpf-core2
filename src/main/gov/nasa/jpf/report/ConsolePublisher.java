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
package gov.nasa.jpf.report;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.util.Left;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.Step;
import gov.nasa.jpf.vm.Transition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//import org.apache.commons.io.FileUtils;

public class ConsolePublisher extends Publisher {

	// output destinations
	String fileName;
	FileOutputStream fos;

	String port;

	// the various degrees of information for program traces
	protected boolean showCG;
	protected boolean showSteps;
	protected boolean showLocation;
	protected boolean showSource;
	protected boolean showMethod;
	protected boolean showCode;

	public ConsolePublisher(Config conf, Reporter reporter) {
		super(conf, reporter);

		// options controlling the output destination
		fileName = conf.getString("report.console.file");
		port = conf.getString("report.console.port");

		// options controlling what info should be included in a trace
		showCG = conf.getBoolean("report.console.show_cg", true);
		showSteps = conf.getBoolean("report.console.show_steps", true);
		showLocation = conf.getBoolean("report.console.show_location", true);
		showSource = conf.getBoolean("report.console.show_source", true);
		showMethod = conf.getBoolean("report.console.show_method", false);
		showCode = conf.getBoolean("report.console.show_code", false);

	}

	@Override
	public String getName() {
		return "console";
	}

	@Override
	protected void openChannel() {

		if (fileName != null) {
			try {
				fos = new FileOutputStream(fileName);
				out = new PrintWriter(fos);
			} catch (FileNotFoundException x) {
				// fall back to System.out
			}
		} else if (port != null) {
			// <2do>
		}

		if (out == null) {
			out = new PrintWriter(System.out, true);
		}
	}

	@Override
	protected void closeChannel() {
		if (fos != null) {
			out.close();
		}
	}

	@Override
	public void publishTopicStart(String topic) {
		out.println();
		out.print("====================================================== ");
		out.println(topic);
	}

	@Override
	public void publishTopicEnd(String topic) {
		// nothing here
	}

	@Override
	public void publishStart() {
		super.publishStart();

		if (startItems.length > 0) { // only report if we have output for this
										// phase
			publishTopicStart("search started: "
					+ formatDTG(reporter.getStartDate()));
		}
	}

	@Override
	public void publishFinished() {
		super.publishFinished();

		if (finishedItems.length > 0) { // only report if we have output for
										// this phase
			publishTopicStart("search finished: "
					+ formatDTG(reporter.getFinishedDate()));
		}
	}

	@Override
	protected void publishJPF() {
		out.println(reporter.getJPFBanner());
		out.println();
	}

	@Override
	protected void publishDTG() {
		out.println("started: " + reporter.getStartDate());
	}

	@Override
	protected void publishUser() {
		out.println("user: " + reporter.getUser());
	}

	@Override
	protected void publishJPFConfig() {
		publishTopicStart("JPF configuration");

		TreeMap<Object, Object> map = conf.asOrderedMap();
		Set<Map.Entry<Object, Object>> eSet = map.entrySet();

		for (Object src : conf.getSources()) {
			out.print("property source: ");
			out.println(conf.getSourceName(src));
		}

		out.println("properties:");
		for (Map.Entry<Object, Object> e : eSet) {
			out.println("  " + e.getKey() + "=" + e.getValue());
		}
	}

	@Override
	protected void publishPlatform() {
		publishTopicStart("platform");
		out.println("hostname: " + reporter.getHostName());
		out.println("arch: " + reporter.getArch());
		out.println("os: " + reporter.getOS());
		out.println("java: " + reporter.getJava());
	}

	@Override
	protected void publishSuT() {
		publishTopicStart("system under test");
		out.println(reporter.getSuT());
	}

	@Override
	protected void publishError() {
		Error e = reporter.getCurrentError();

		publishTopicStart("error " + e.getId());
		out.println(e.getDescription());

		String s = e.getDetails();
		if (s != null) {
			out.println(s);
		}

	}

	@Override
	protected void publishConstraint() {
		String constraint = reporter.getLastSearchConstraint();
		publishTopicStart("search constraint");
		out.println(constraint); // not much info here yet
	}

	@Override
	protected void publishResult() {
		List<Error> errors = reporter.getErrors();

		publishTopicStart("results");

		if (errors.isEmpty()) {
			out.println("no errors detected");
		} else {
			for (Error e : errors) {
				out.print("error #");
				out.print(e.getId());
				out.print(": ");
				out.print(e.getDescription());

				String s = e.getDetails();
				if (s != null) {
					s = s.replace('\n', ' ');
					s = s.replace('\t', ' ');
					s = s.replace('\r', ' ');
					out.print(" \"");
					if (s.length() > 50) {
						out.print(s.substring(0, 50));
						out.print("...");
					} else {
						out.print(s);
					}
					out.print('"');
				}

				out.println();
			}
		}
	}

	/**
	 * this is done as part of the property violation reporting, i.e. we have an
	 * error
	 */
	@Override
	protected void publishTrace() {
		Path path = reporter.getPath();
		int i = 0;

		if (path.size() == 0) {
			return; // nothing to publish
		}

		publishTopicStart("trace " + reporter.getCurrentErrorId());

		for (Transition t : path) {
			out.print("------------------------------------------------------ ");
			out.println("transition #" + i++ + " thread: " + t.getThreadIndex());

			if (showCG) {
				out.println(t.getChoiceGenerator());
			}

			if (showSteps) {
				String lastLine = null;
				MethodInfo lastMi = null;
				int nNoSrc = 0;

				for (Step s : t) {
					if (showSource) {
						String line = s.getLineString();
						if (line != null) {
							if (!line.equals(lastLine)) {
								if (nNoSrc > 0) {
									out.println("      [" + nNoSrc
											+ " insn w/o sources]");
								}

								out.print("  ");
								if (showLocation) {
									out.print(Left.format(
											s.getLocationString(), 30));
									out.print(" : ");
								}
								out.println(line.trim());
								nNoSrc = 0;

							}
						} else { // no source
							nNoSrc++;
						}

						lastLine = line;
					}

					if (showCode) {
						Instruction insn = s.getInstruction();
						if (showMethod) {
							MethodInfo mi = insn.getMethodInfo();
							if (mi != lastMi) {
								ClassInfo mci = mi.getClassInfo();
								out.print("    ");
								if (mci != null) {
									out.print(mci.getName());
									out.print(".");
								}
								out.println(mi.getUniqueName());
								lastMi = mi;
							}
						}
						out.print("      ");
						out.println(insn);
					}
				}

				if (showSource && !showCode && (nNoSrc > 0)) {
					out.println("      [" + nNoSrc + " insn w/o sources]");
				}
			}
		}
	}

	@Override
	protected void publishOutput() {
		Path path = reporter.getPath();

		if (path.size() == 0) {
			return; // nothing to publish
		}

		publishTopicStart("output " + reporter.getCurrentErrorId());

		if (path.hasOutput()) {
			for (Transition t : path) {
				String s = t.getOutput();
				if (s != null) {
					out.print(s);
				}
			}
		} else {
			out.println("no output");
		}
	}

	@Override
	protected void publishSnapshot() {
		VM vm = reporter.getVM();

		// not so nice - we have to delegate this since it's using a lot of
		// internals, and is also
		// used in debugging
		publishTopicStart("snapshot " + reporter.getCurrentErrorId());

		if (vm.getPathLength() > 0) {
			vm.printLiveThreadStatus(out);
		} else {
			out.println("initial program state");
		}
	}

	public static final String STATISTICS_TOPIC = "statistics";

	// this is useful if somebody wants to monitor progress from a specialized
	// ConsolePublisher
	public synchronized void printStatistics(PrintWriter pw) {
		publishTopicStart(STATISTICS_TOPIC);
		printStatistics(pw, reporter);
	}

	// this can be used outside a publisher, to show the same info
	public static void printStatistics(PrintWriter pw, Reporter reporter) {
		Statistics stat = reporter.getStatistics();
		// File file = new File("C:/Users/kuja/Test output/file.txt");
		// file.getParentFile().mkdirs();
		// PrintWriter printWriter = new PrintWriter(file);

		pw.println("elapsed time:       "
				+ formatHMS(reporter.getElapsedTime()));
		pw.println("states:             new=" + stat.newStates + ",visited="
				+ stat.visitedStates + ",backtracked=" + stat.backtracked
				+ ",end=" + stat.endStates);
		pw.println("search:             maxDepth=" + stat.maxDepth
				+ ",constraints=" + stat.constraints);
		pw.println("choice generators:  thread=" + stat.threadCGs + " (signal="
				+ stat.signalCGs + ",lock=" + stat.monitorCGs + ",sharedRef="
				+ stat.sharedAccessCGs + ",threadApi=" + stat.threadApiCGs
				+ ",reschedule=" + stat.breakTransitionCGs + "), data="
				+ stat.dataCGs);
		pw.println("heap:               " + "new=" + stat.nNewObjects
				+ ",released=" + stat.nReleasedObjects + ",maxLive="
				+ stat.maxLiveObjects + ",gcCycles=" + stat.gcCycles);
		pw.println("instructions:       " + stat.insns);
		pw.println("max memory:         " + (stat.maxUsed >> 20) + "MB");

		pw.println("loaded code:        classes="
				+ ClassLoaderInfo.getNumberOfLoadedClasses() + ",methods="
				+ MethodInfo.getNumberOfLoadedMethods());
	}

	/*
	public synchronized void printToFile(PrintWriter pw) {
		printToFile(pw, reporter);
	}

	public static void printToFile(PrintWriter pw, Reporter reporter) {
		int count;
		int countF;
		File dir = new File(System.getProperty("user.home") + "/TestTime/");
		if (!dir.exists()) {
			if (dir.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}
		try {
			File finalBFS = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BFSfinal_result.txt");
			if (!finalBFS.exists()) {
				finalBFS.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalBFS);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}
			File finalBFSD = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BFSfinal_Depth.txt");
			if (!finalBFSD.exists()) {
				finalBFSD.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalBFSD);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}
			File finalDFS = new File(System.getProperty("user.home")
					+ "/TestTime/" + "DFSfinal_result.txt");
			if (!finalDFS.exists()) {
				finalDFS.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalDFS);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}
			File finalDFSD = new File(System.getProperty("user.home")
					+ "/TestTime/" + "DFSfinal_Depth.txt");
			if (!finalDFSD.exists()) {
				finalDFSD.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalDFSD);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}

			File finalBC = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BCfinal_result.txt");
			if (!finalBC.exists()) {
				finalBC.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalBC);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}
			
			File finalBCD = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BCfinal_Depth.txt");
			if (!finalBCD.exists()) {
				finalBCD.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalBCD);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}

			File finalCBC = new File(System.getProperty("user.home")
					+ "/TestTime/" + "CBCfinal_result.txt");
			if (!finalCBC.exists()) {
				finalCBC.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalCBC);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}
			
			File finalCBCD = new File(System.getProperty("user.home")
					+ "/TestTime/" + "CBCfinal_Depth.txt");
			if (!finalCBCD.exists()) {
				finalCBCD.createNewFile();
				FileOutputStream fos = new FileOutputStream(finalCBCD);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				System.out.println(0);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		count = 1;
		countF = 1;
		String search = reporter.jpf.getSearch().toString();
		List<Error> errors = reporter.getErrors();
		//
		if (search.contains("gov.nasa.jpf.search.heuristic.BFSHeuristic")) {
			File file2 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BFSLog" + count + ".txt");
			File file4 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BFSDepth" + count + ".txt");

			while (file2.exists()) {
				count++;
				file2 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "BFSLog" + count + ".txt");
				file4 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "BFSDepth" + count + ".txt");
			}

			try {
				FileOutputStream fos = new FileOutputStream(file2);
				PrintStream ps = new PrintStream(fos);

				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println((reporter.getElapsedTime()));
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

				try {
					fos.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

			try {

				FileOutputStream fos;
				fos = new FileOutputStream(file4);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println(reporter.stat.maxDepth);
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// read the file as string
			try {

				// files to read
				File file = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BFSLog" + countF + ".txt");
				File file3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BFSDepth" + countF + ".txt");
				// file to write
				File catFile = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BFSfinal_result.txt");
				File catFile3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BFSfinal_Depth.txt");

				String fileStr = FileUtils.readFileToString(file);
				String fileStr3 = FileUtils.readFileToString(file3);

				FileUtils.write(catFile, fileStr);
				FileUtils.write(catFile3, fileStr3);

				while (file.exists()) {

					countF++;
					file = new File(System.getProperty("user.home")
							+ "/TestTime/" + "BFSLog" + countF + ".txt");
					file3 = new File(System.getProperty("user.home")
							+ "/TestTime/" + "BFSDepth" + countF + ".txt");

					if (!file.exists()) {
						break;
					}
					fileStr = FileUtils.readFileToString(file);
					fileStr3 = FileUtils.readFileToString(file3);

					FileUtils.write(catFile, fileStr, true);
					FileUtils.write(catFile3, fileStr3, true);

				}

			} catch (IOException e) {
				System.err.println("An IOException was caught!");
				e.printStackTrace();
			}
		}
		
		if (search.contains("gov.nasa.jpf.search.heuristic.DFSHeuristic")) {
			File file2 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "DFSLog" + count + ".txt");
			File file4 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "DFSDepth" + count + ".txt");

			while (file2.exists()) {
				count++;
				file2 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "DFSLog" + count + ".txt");
				file4 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "DFSDepth" + count + ".txt");
			}

			try {
				FileOutputStream fos = new FileOutputStream(file2);
				PrintStream ps = new PrintStream(fos);

				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println((reporter.getElapsedTime()));
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

				try {
					fos.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

			try {

				FileOutputStream fos;
				fos = new FileOutputStream(file4);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println(reporter.stat.maxDepth);
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// read the file as string
			try {

				// files to read
				File file = new File(System.getProperty("user.home")
						+ "/TestTime/" + "DFSLog" + countF + ".txt");
				File file3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "DFSDepth" + countF + ".txt");
				// file to write
				File catFile = new File(System.getProperty("user.home")
						+ "/TestTime/" + "DFSfinal_result.txt");
				File catFile3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "DFSfinal_Depth.txt");

				String fileStr = FileUtils.readFileToString(file);
				String fileStr3 = FileUtils.readFileToString(file3);

				FileUtils.write(catFile, fileStr);
				FileUtils.write(catFile3, fileStr3);

				while (file.exists()) {

					countF++;
					file = new File(System.getProperty("user.home")
							+ "/TestTime/" + "DFSLog" + countF + ".txt");
					file3 = new File(System.getProperty("user.home")
							+ "/TestTime/" + "DFSDepth" + countF + ".txt");

					if (!file.exists()) {
						break;
					}
					fileStr = FileUtils.readFileToString(file);
					fileStr3 = FileUtils.readFileToString(file3);

					FileUtils.write(catFile, fileStr, true);
					FileUtils.write(catFile3, fileStr3, true);

				}

			} catch (IOException e) {
				System.err.println("An IOException was caught!");
				e.printStackTrace();
			}
		}
		if (search.contains("gov.nasa.jpf.search.heuristic.BranchCountHeuristic")) {
			File file2 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BCLog" + count + ".txt");
			File file4 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "BCDepth" + count + ".txt");

			while (file2.exists()) {
				count++;
				file2 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "BCLog" + count + ".txt");
				file4 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "BCDepth" + count + ".txt");
			}

			try {
				FileOutputStream fos = new FileOutputStream(file2);
				PrintStream ps = new PrintStream(fos);

				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println((reporter.getElapsedTime()));
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

				try {
					fos.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

			try {

				FileOutputStream fos;
				fos = new FileOutputStream(file4);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println(reporter.stat.maxDepth);
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// read the file as string
			try {

				// files to read
				File file = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BCLog" + countF + ".txt");
				File file3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BCDepth" + countF + ".txt");
				// file to write
				File catFile = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BCfinal_result.txt");
				File catFile3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "BCfinal_Depth.txt");

				String fileStr = FileUtils.readFileToString(file);
				String fileStr3 = FileUtils.readFileToString(file3);

				FileUtils.write(catFile, fileStr);
				FileUtils.write(catFile3, fileStr3);

				while (file.exists()) {

					countF++;
					file = new File(System.getProperty("user.home")
							+ "/TestTime/" + "BCLog" + countF + ".txt");
					file3 = new File(System.getProperty("user.home")
							+ "/TestTime/" + "BCDepth" + countF + ".txt");

					if (!file.exists()) {
						break;
					}
					fileStr = FileUtils.readFileToString(file);
					fileStr3 = FileUtils.readFileToString(file3);

					FileUtils.write(catFile, fileStr, true);
					FileUtils.write(catFile3, fileStr3, true);

				}

			} catch (IOException e) {
				System.err.println("An IOException was caught!");
				e.printStackTrace();
			}
		}
		if (search.contains("gov.nasa.jpf.search.heuristic.CombinatorialBranchCountHeuristic")) {
			File file2 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "CBCLog" + count + ".txt");
			File file4 = new File(System.getProperty("user.home")
					+ "/TestTime/" + "CBCDepth" + count + ".txt");

			while (file2.exists()) {
				count++;
				file2 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "CBCLog" + count + ".txt");
				file4 = new File(System.getProperty("user.home") + "/TestTime/"
						+ "CBCDepth" + count + ".txt");
			}

			try {
				FileOutputStream fos = new FileOutputStream(file2);
				PrintStream ps = new PrintStream(fos);

				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println((reporter.getElapsedTime()));
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

				try {
					fos.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

			try {

				FileOutputStream fos;
				fos = new FileOutputStream(file4);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);

				if (!errors.isEmpty()) {
					System.out.println(reporter.stat.maxDepth);
				}
				if (errors.isEmpty()) {
					int n = 0;
					System.out.print(n);
				}

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// read the file as string
			try {

				// files to read
				File file = new File(System.getProperty("user.home")
						+ "/TestTime/" + "CBCLog" + countF + ".txt");
				File file3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "CBCDepth" + countF + ".txt");
				// file to write
				File catFile = new File(System.getProperty("user.home")
						+ "/TestTime/" + "CBCfinal_result.txt");
				File catFile3 = new File(System.getProperty("user.home")
						+ "/TestTime/" + "CBCfinal_Depth.txt");

				String fileStr = FileUtils.readFileToString(file);
				String fileStr3 = FileUtils.readFileToString(file3);

				FileUtils.write(catFile, fileStr);
				FileUtils.write(catFile3, fileStr3);

				while (file.exists()) {

					countF++;
					file = new File(System.getProperty("user.home")
							+ "/TestTime/" + "CBCLog" + countF + ".txt");
					file3 = new File(System.getProperty("user.home")
							+ "/TestTime/" + "CBCDepth" + countF + ".txt");

					if (!file.exists()) {
						break;
					}
					fileStr = FileUtils.readFileToString(file);
					fileStr3 = FileUtils.readFileToString(file3);

					FileUtils.write(catFile, fileStr, true);
					FileUtils.write(catFile3, fileStr3, true);

				}

			} catch (IOException e) {
				System.err.println("An IOException was caught!");
				e.printStackTrace();
			}
		}
	}
	
*/

	@Override
	public void publishStatistics() {
		printStatistics(out);
//		printToFile(out, reporter);
	}

}
