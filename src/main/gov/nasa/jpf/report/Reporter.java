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
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Path;

//import org.apache.commons.io.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * this is our default report generator, which is heavily configurable via our
 * standard properties. Note this gets instantiated and registered automatically
 * via JPF.addListeners(), so you don't have to add it explicitly
 */

public class Reporter extends SearchListenerAdapter {
	

	public static Logger log = JPF.getLogger("report");

	protected Config conf;
	protected JPF jpf;
	protected Search search;
	protected VM vm;

	protected Date started, finished;
	protected Statistics stat; // the object that collects statistics
	protected List<Publisher> publishers = new ArrayList<Publisher>();

	protected Thread probeTimer;
	
	static int count;
	static int countF;
	
	public Reporter(Config conf, JPF jpf) {
		this.conf = conf;
		this.jpf = jpf;
		search = jpf.getSearch();
		vm = jpf.getVM();
		boolean reportStats = conf.getBoolean("report.statistics", false);

		started = new Date();

		addConfiguredPublishers(conf);

		for (Publisher publisher : publishers) {
			if (reportStats || publisher.hasToReportStatistics()) {
				reportStats = true;
			}

			if (publisher instanceof JPFListener) {
				jpf.addListener((JPFListener) publisher);
			}
		}

		if (reportStats) {
			getRegisteredStatistics();
		}

		int probeInterval = conf.getInt("report.probe_interval");
		if (probeInterval > 0) {
			probeTimer = createProbeIntervalTimer(probeInterval);
		}
	}

	protected Thread createProbeIntervalTimer(final int probeInterval) {
		Thread timer = new Thread(new Runnable() {
			@Override
			public void run() {
				log.info("probe timer running");
				while (!search.isDone()) {
					try {
						Thread.sleep(probeInterval * 1000);
						search.probeSearch(); // this is only a request
					} catch (InterruptedException ix) {
						// nothing
					}
				}
				log.info("probe timer terminating");
			}
		}, "probe-timer");
		timer.setDaemon(true);

		// we don't start before the Search is started

		return timer;
	}
	
	//MOD - Made it easier to use the elapsed time format
	static char[] tBuf = { '0', '0', ':', '0', '0', ':', '0', '0' };

	static synchronized public String formatHMS(long t) {
		int h = (int) (t / 3600000);
		int m = (int) ((t / 60000) % 60);
		int s = (int) ((t / 1000) % 60);

		tBuf[0] = (char) ('0' + (h / 10));
		tBuf[1] = (char) ('0' + (h % 10));

		tBuf[3] = (char) ('0' + (m / 10));
		tBuf[4] = (char) ('0' + (m % 10));

		tBuf[6] = (char) ('0' + (s / 10));
		tBuf[7] = (char) ('0' + (s % 10));

		return new String(tBuf);
	}

	
	/**
	 * called after the JPF run is finished. Shouldn't be public, but is called
	 * by JPF
	 */
	//MOD - Added prompts and create file 
	//By default saves in the user home /TestSaves directory
	//This method usually does nothing
	public void cleanUp () {
		// nothing yet
		
//		File dir = new File(System.getProperty("user.home") + "/TestTime/");
//		if (!dir.exists()) {
//			if (dir.mkdir()) {
//				System.out.println("Directory is created!");
//			} else {
//				System.out.println("Failed to create directory!");
//			}
//		}
//		try {
//		File finalBFS = new File(System.getProperty("user.home") + "/TestTime/" + "BFSfinal_result.txt");
//		if (!finalBFS.exists()) {
//			finalBFS.createNewFile();
//			FileOutputStream fos = new FileOutputStream(finalBFS);
//			PrintStream ps = new PrintStream(fos);	
//			System.setOut(ps);
//			System.out.println(0);
//		}
//		File finalBFSD = new File(System.getProperty("user.home") + "/TestTime/" + "BFSfinal_Depth.txt");
//		if (!finalBFSD.exists()) {
//			finalBFSD.createNewFile();
//			FileOutputStream fos = new FileOutputStream(finalBFSD);
//			PrintStream ps = new PrintStream(fos);	
//			System.setOut(ps);
//			System.out.println(0);
//		}
//		File finalDFS = new File(System.getProperty("user.home") + "/TestTime/" + "DFSfinal_result.txt");
//		if (!finalDFS.exists()) {
//			finalDFS.createNewFile();
//			FileOutputStream fos = new FileOutputStream(finalDFS);
//			PrintStream ps = new PrintStream(fos);	
//			System.setOut(ps);
//			System.out.println(0);
//		}
//		File finalDFSD = new File(System.getProperty("user.home") + "/TestTime/" + "DFSfinal_Depth.txt");
//		if (!finalDFSD.exists()) {
//			finalDFSD.createNewFile();
//			FileOutputStream fos = new FileOutputStream(finalDFSD);
//			PrintStream ps = new PrintStream(fos);	
//			System.setOut(ps);
//			System.out.println(0);
//		}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		count=1;
//		countF = 1;
//		String search = jpf.getSearch().toString();
//		List<Error> errors = getErrors();
//		
//		if(search.contains("gov.nasa.jpf.search.heuristic.BFSHeuristic")){
//			File file2 = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSLog" + count +".txt");
//			File file4 = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSDepth" + count +".txt");
//			
//			while(file2.exists()){
//				count++;
//				file2 = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSLog" + count +".txt");
//				file4 = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSDepth" + count +".txt");
//			}	
//			
//			try {
//				FileOutputStream fos = new FileOutputStream(file2);
//				PrintStream ps = new PrintStream(fos);
//				
//				System.setOut(ps);
//				
//					if (!errors.isEmpty()) {
//						System.out.println((getElapsedTime()));
//					}
//					if (errors.isEmpty()) {
//						int n = 0;
//						System.out.print(n);
//					}
//			
//			try {
//				fos.close();
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			}
//			
//			} catch (FileNotFoundException e) {
//				
//				e.printStackTrace();
//			}
//			
//			
//			try {
//				
//				FileOutputStream fos;
//				fos = new FileOutputStream(file4);
//				PrintStream ps = new PrintStream(fos);
//				System.setOut(ps);
//				
//				if (!errors.isEmpty()) {
//					System.out.println(stat.maxDepth);
//				}
//				if (errors.isEmpty()) {
//					int n = 0;
//					System.out.print(n);
//				}
//				
//				
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			
//			
//			
//			
//			
//			// read the file as string
//			try {
//				
//				// files to read
//				File file = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSLog"+ countF +".txt");
//				File file3 = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSDepth"+ countF +".txt");
//				// file to write
//				File catFile = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSfinal_result.txt");
//				File catFile3 = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSfinal_Depth.txt"); 
//				
//				String fileStr = FileUtils.readFileToString(file);
//				String fileStr3 = FileUtils.readFileToString(file3);
//				
//				FileUtils.write(catFile, fileStr);
//				FileUtils.write(catFile3, fileStr3);
//				
//				while(file.exists()){
//					
//					countF++;
//					file = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSLog" + countF +".txt");
//					file3 = new File(System.getProperty("user.home")  + "/TestTime/" + "BFSDepth" + countF +".txt");
//					
//					if(!file.exists()){	
//						break;
//					}
//					fileStr = FileUtils.readFileToString(file);
//					fileStr3 = FileUtils.readFileToString(file3);
//					
//					FileUtils.write(catFile, fileStr, true);
//					FileUtils.write(catFile3, fileStr3, true);
//					
//				}
//
//			} catch (IOException e) {
//				System.err.println("An IOException was caught!");
//				e.printStackTrace();
//			}
//		}
//		
//		if(search.contains("gov.nasa.jpf.search.heuristic.DFSHeuristic")){
//			File file2 = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSLog" + count +".txt");
//			File file4 = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSDepth" + count +".txt");
//			
//			while(file2.exists()){
//				count++;
//				file2 = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSLog" + count +".txt");
//				file4 = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSDepth" + count +".txt");
//			}	
//			
//			try {
//				FileOutputStream fos = new FileOutputStream(file2);
//				PrintStream ps = new PrintStream(fos);
//				
//				System.setOut(ps);
//				
//					if (!errors.isEmpty()) {
//						System.out.println((getElapsedTime()));
//					}
//					if (errors.isEmpty()) {
//						int n = 0;
//						System.out.print(n);
//					}
//			
//			try {
//				fos.close();
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			}
//			
//			} catch (FileNotFoundException e) {
//				
//				e.printStackTrace();
//			}
//			
//			
//			try {
//				
//				FileOutputStream fos;
//				fos = new FileOutputStream(file4);
//				PrintStream ps = new PrintStream(fos);
//				System.setOut(ps);
//				
//				if (!errors.isEmpty()) {
//					System.out.println(stat.maxDepth);
//				}
//				if (errors.isEmpty()) {
//					int n = 0;
//					System.out.print(n);
//				}
//				
//				
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			
//			
//			
//			
//			
//			// read the file as string
//			try {
//				
//				// files to read
//				File file = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSLog"+ countF +".txt");
//				File file3 = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSDepth"+ countF +".txt");
//				// file to write
//				File catFile = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSfinal_result.txt");
//				File catFile3 = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSfinal_Depth.txt"); 
//				
//				String fileStr = FileUtils.readFileToString(file);
//				String fileStr3 = FileUtils.readFileToString(file3);
//				
//				FileUtils.write(catFile, fileStr);
//				FileUtils.write(catFile3, fileStr3);
//				
//				while(file.exists()){
//					
//					countF++;
//					file = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSLog" + countF +".txt");
//					file3 = new File(System.getProperty("user.home")  + "/TestTime/" + "DFSDepth" + countF +".txt");
//					
//					if(!file.exists()){	
//						break;
//					}
//					fileStr = FileUtils.readFileToString(file);
//					fileStr3 = FileUtils.readFileToString(file3);
//					
//					FileUtils.write(catFile, fileStr, true);
//					FileUtils.write(catFile3, fileStr3, true);
//					
//				}
//
//			} catch (IOException e) {
//				System.err.println("An IOException was caught!");
//				e.printStackTrace();
//			}
//		}
		
	}
	
	public Statistics getRegisteredStatistics() {

		if (stat == null) { // none yet, initialize
			// first, check if somebody registered one explicitly
			stat = vm.getNextListenerOfType(Statistics.class, null);
			if (stat == null) {
				stat = conf.getInstance("report.statistics.class@stat",
						Statistics.class);
				if (stat == null) {
					stat = new Statistics();
				}
				jpf.addListener(stat);
			}
		}

		return stat;
	}

	void addConfiguredPublishers(Config conf) {
		String[] def = { "console" };

		Class<?>[] argTypes = { Config.class, Reporter.class };
		Object[] args = { conf, this };

		for (String id : conf.getStringArray("report.publisher", def)) {
			Publisher p = conf.getInstance("report." + id + ".class",
					Publisher.class, argTypes, args);
			if (p != null) {
				publishers.add(p);
			} else {
				log.warning("could not instantiate publisher class: " + id);
			}
		}
	}

	public void addPublisher(Publisher newPublisher) {
		publishers.add(newPublisher);
	}

	public List<Publisher> getPublishers() {
		return publishers;
	}

	public boolean hasToReportTrace() {
		for (Publisher p : publishers) {
			if (p.hasTopic("trace")) {
				return true;
			}
		}

		return false;
	}

	public boolean hasToReportOutput() {
		for (Publisher p : publishers) {
			if (p.hasTopic("output")) {
				return true;
			}
		}

		return false;
	}

	public <T extends Publisher> boolean addPublisherExtension(
			Class<T> publisherCls, PublisherExtension e) {
		boolean added = false;
		for (Publisher p : publishers) {
			Class<?> pCls = p.getClass();
			if (publisherCls.isAssignableFrom(pCls)) {
				p.addExtension(e);
				added = true;
			}
		}

		return added;
	}

	public <T extends Publisher> void setPublisherItems(Class<T> publisherCls,
			int category, String[] topics) {
		for (Publisher p : publishers) {
			if (publisherCls.isInstance(p)) {
				p.setItems(category, topics);
				return;
			}
		}
	}

	boolean contains(String key, String[] list) {
		for (String s : list) {
			if (s.equalsIgnoreCase(key)) {
				return true;
			}
		}
		return false;
	}

	// --- the publishing phases

	protected void publishStart() {
		for (Publisher publisher : publishers) {
			publisher.openChannel();
			publisher.publishProlog();
			publisher.publishStart();
		}
	}

	protected void publishTransition() {
		for (Publisher publisher : publishers) {
			publisher.publishTransition();
		}
	}

	protected void publishPropertyViolation() {
		for (Publisher publisher : publishers) {
			publisher.publishPropertyViolation();
		}
	}

	protected void publishConstraintHit() {
		for (Publisher publisher : publishers) {
			publisher.publishConstraintHit();
		}
	}

	protected void publishFinished() {
		for (Publisher publisher : publishers) {
			publisher.publishFinished();
			publisher.publishEpilog();
			publisher.closeChannel();
		}
	}

	protected void publishProbe() {
		for (Publisher publisher : publishers) {
			publisher.publishProbe();
		}
	}

	// --- the listener interface that drives report generation

	@Override
	public void searchStarted(Search search) {
		publishStart();

		if (probeTimer != null) {
			probeTimer.start();
		}
	}

	@Override
	public void stateAdvanced(Search search) {
		publishTransition();
	}

	@Override
	public void searchConstraintHit(Search search) {
		publishConstraintHit();
	}

	@Override
	public void searchProbed(Search search) {
		publishProbe();
	}

	@Override
	public void propertyViolated(Search search) {
		publishPropertyViolation();
	}

	@Override
	public void searchFinished(Search search) {
		finished = new Date();

		publishFinished();

		if (probeTimer != null) {
			// we could interrupt, but it's a daemon anyways
			probeTimer = null;
		}
	}

	// --- various getters

	public Date getStartDate() {
		return started;
	}

	public Date getFinishedDate() {
		return finished;
	}

	public VM getVM() {
		return vm;
	}

	public Search getSearch() {
		return search;
	}

	public List<Error> getErrors() {
		return search.getErrors();
	}

	public Error getCurrentError() {
		return search.getCurrentError();
	}

	public String getLastSearchConstraint() {
		return search.getLastSearchConstraint();
	}

	public String getCurrentErrorId() {
		Error e = getCurrentError();
		if (e != null) {
			return "#" + e.getId();
		} else {
			return "";
		}
	}

	public int getNumberOfErrors() {
		return search.getErrors().size();
	}

	public Statistics getStatistics() {
		return stat;
	}

	public Statistics getStatisticsSnapshot() {
		return stat.clone();
	}

	/**
	 * in ms
	 */
	public long getElapsedTime() {
		Date d = (finished != null) ? finished : new Date();
		long t = d.getTime() - started.getTime();
		return t;
	}

	public Path getPath() {
		return vm.getClonedPath();
	}

	public String getJPFBanner() {
		StringBuilder sb = new StringBuilder();

		sb.append("JavaPathfinder v");
		sb.append(JPF.VERSION);

		String rev = getRevision();
		if (rev != null) {
			sb.append(" (rev ");
			sb.append(rev);
			sb.append(')');
		}

		sb.append(" - (C) RIACS/NASA Ames Research Center");

		if (conf.getBoolean("report.show_repository", false)) {
			String repInfo = getRepositoryInfo();
			if (repInfo != null) {
				sb.append(repInfo);
			}
		}

		return sb.toString();
	}

	protected String getRevision() {
		try {
			InputStream is = JPF.class.getResourceAsStream(".version");
			if (is != null) {
				int len = is.available();
				byte[] data = new byte[len];
				is.read(data);
				is.close();
				return new String(data).trim();

			} else {
				return null;
			}

		} catch (Throwable t) {
			return null;
		}
	}

	protected String getRepositoryInfo() {
		try {
			InputStream is = JPF.class.getResourceAsStream("build.properties");
			if (is != null) {
				Properties revInfo = new Properties();
				revInfo.load(is);

				StringBuffer sb = new StringBuffer();
				String date = revInfo.getProperty("date");
				String author = revInfo.getProperty("author");
				String rev = revInfo.getProperty("rev");
				String machine = revInfo.getProperty("hostname");
				String loc = revInfo.getProperty("location");
				String upstream = revInfo.getProperty("upstream");

				return String.format("%s %s %s %s %s", date, author, rev,
						machine, loc);
			}
		} catch (IOException iox) {
			return null;
		}

		return null;
	}

	public String getHostName() {
		try {
			InetAddress in = InetAddress.getLocalHost();
			String hostName = in.getHostName();
			return hostName;
		} catch (Throwable t) {
			return "localhost";
		}
	}

	public String getUser() {
		return System.getProperty("user.name");
	}

	public String getSuT() {
		return vm.getSUTDescription();
	}

	public String getJava() {
		String vendor = System.getProperty("java.vendor");
		String version = System.getProperty("java.version");
		return vendor + "/" + version;
	}

	public String getArch() {
		String arch = System.getProperty("os.arch");
		Runtime rt = Runtime.getRuntime();
		String type = arch + "/" + rt.availableProcessors();

		return type;
	}

	public String getOS() {
		String name = System.getProperty("os.name");
		String version = System.getProperty("os.version");
		return name + "/" + version;
	}

}
