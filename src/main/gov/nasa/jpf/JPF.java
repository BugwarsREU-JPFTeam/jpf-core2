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
package gov.nasa.jpf;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.VMListener;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.report.Reporter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.util.LogManager;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.ObjArray;
import gov.nasa.jpf.util.RunRegistry;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;


/**
 * main class of the JPF verification framework. This reads the configuration,
 * instantiates the Search and VM objects, and kicks off the Search
 */
public class JPF implements Runnable {
  
  /** JPF version, we read this in later from default.properties */
  public static String VERSION    = "4.?";

  static Logger logger     = null; // initially

  public enum Status { NEW, RUNNING, DONE };

  class ConfigListener implements ConfigChangeListener {
    public void propertyChanged(Config config, String key, String oldValue, String newValue) {
            
      //--- check for new listeners
      if ("jpf.listener".equals(key)) {
        String[] nv = config.asStringArray(newValue);
        String[] ov = config.asStringArray(oldValue);
        String[] newListeners = Misc.getAddedElements(ov, nv);
        
        if (newListeners != null) {
          for (String clsName : newListeners) {
            try {
              JPFListener newListener = config.getInstance("jpf.listener", clsName, JPFListener.class);
              addListener(newListener);
              logger.info("config changed, added jpf.listener " + clsName);

            } catch (JPFConfigException cfx) {
              logger.warning("jpf.listener change failed: " + cfx.getMessage());
            }
          }
        }
      }
    }    
  }
  
  /** this is the backbone of all JPF configuration */
  Config config;

  /** The search policy used to explore the state space */
  Search search;

  /** Reference to the virtual machine used by the search */
  JVM vm;

  /** the report generator */
  Reporter reporter;

  Status status = Status.NEW;
  
  /** we use this as safety margin, to be released upon OutOfMemoryErrors */
  byte[] memoryReserve;
  
  private static Logger initLogging(Config conf) {
    LogManager.init(conf);
    return getLogger("gov.nasa.jpf");
  }

  /**
   * use this one to get a Logger that is initialized via our Config mechanism. Note that
   * our own Loggers do NOT pass
   */
  public static Logger getLogger (String name) {
    return LogManager.getLogger( name);
  }

  public static void main(String[] args) {
    String[] origArgs = (String[]) args.clone();

    try {
      Config conf = createConfig(args);

      if (logger == null) {
        logger = initLogging(conf);
      }

      if (isHelpRequest(args)) {
        showUsage();
        return;
      }

      // this is redundant to jpf.report.<publisher>.start=..config..
      // but nobody can remember this (it's only used to produce complete
      // reports)
      if (isPrintConfigRequest(args)) {
        conf.print(new PrintWriter(System.out));
      }

      setConfigPaths(conf);

      // check if there is a shell class specification, in which case we just
      // delegate
      try {
        Class<?> shellCls = conf.getClass("jpf.shell.class");
        if (shellCls != null) {
          Method m = shellCls.getMethod("main", String[].class);
          if (m != null) {
            m.invoke(null, (Object) origArgs);
            return;
          } else {
            logger.severe("shell class has no main() method: " + shellCls.getName());
          }
        }
      } catch (Throwable t) {
        logger.severe("exception during shell initialization: " + t);
        t.printStackTrace();
        return;
      }

      // no shell, we start JPF directly
      LogManager.printStatus(logger);
      conf.printStatus(logger);

      // this has to be done after we checked&consumed all "-.." arguments
      // this is NOT about checking properties!
      checkUnknownArgs(args);

      try {
        JPF jpf = new JPF(conf);
        jpf.run();
      } catch (ExitException x) {
        if (x.shouldReport()) {
          x.printStackTrace();
        }
      }

    } catch (JPFConfigException cx) {
      System.err.println("configuration error: " + cx.getMessage());
      // shall we print the stacktrace ?
    }
  }

  static void setConfigPaths(Config conf) {
    JPFSite site = JPFSite.getSite();

    //conf.printEntries();

    String[] extensions = conf.getStringArray("extensions");
    if (extensions != null){
      for (String extDir : extensions){
        site.addExtensionDir(extDir);
      }
    }

    ClassLoader cl = JPF.class.getClassLoader();
    if (cl instanceof JPFClassLoader){
      JPFClassLoader jpfCl = (JPFClassLoader)cl;

      // check if there was an explicit 'native_classpath' setting
      String[] nativeCp = conf.getStringArray("native_classpath");
      if (nativeCp != null){
        for (String cpe : nativeCp){
          File f = new File(cpe);
          if (f.exists()){
            try {
              jpfCl.addURL(f.toURI().toURL());
            } catch (MalformedURLException x){
              // can't happen, eisting file
            }
          }
        }
      }


      int i=0;
      for (URL url: site.getNativeCpURLs()){
        if (i++ > 0){ // the first one is already in there
          jpfCl.addURL(url);
        }
      }
    }

  }

  /**
   * create new JPF object. Note this is always guaranteed to return, but the
   * Search and/or VM object instantiation might have failed (i.e. the JPF
   * object might not be really usable). If you directly use getSearch() / getVM(),
   * check for null
   */
  public JPF(Config conf) {
    config = conf;

    if (logger == null) { // maybe somebody created a JPF object explicitly
      logger = initLogging(config);
    }

    String tgt = config.getTarget();
    if (tgt == null || (tgt.length() == 0)) {
      logger.severe("no target class specified, terminating");
    } else {
      initialize();
    }
  }

  /**
   * convenience method if caller doesn't care about Config
   */
  public JPF (String[] args) {
    this( createConfig(args));
  }
  
  private void initialize() {
    VERSION = config.getString("jpf.version", VERSION);
    memoryReserve = new byte[config.getInt("jpf.memory_reserve", 64 * 1024)]; // in bytes
    
    try {
      
      Class<?>[] vmArgTypes = { JPF.class, Config.class };
      Object[] vmArgs = { this, config };
      vm = config.getEssentialInstance("vm.class", JVM.class, vmArgTypes, vmArgs);

      Class<?>[] searchArgTypes = { Config.class, JVM.class };
      Object[] searchArgs = { config, vm };
      search = config.getEssentialInstance("search.class", Search.class,
                                                searchArgTypes, searchArgs);

      addListeners();
      config.addChangeListener(new ConfigListener());
      
    } catch (JPFConfigException cx) {
      logger.severe(cx.toString());
      throw new ExitException(false);
    }
  }  

  public Status getStatus() {
    return status;
  }
  
  public boolean isRunnable () {
    return ((vm != null) && (search != null));
  }

  public void addPropertyListener (PropertyListenerAdapter pl) {
    if (vm != null) {
      vm.addListener( pl);
    }
    if (search != null) {
      search.addListener( pl);
      search.addProperty(pl);
    }
  }

  public void addSearchListener (SearchListener l) {
    if (search != null) {
      search.addListener(l);
    }
  }

  public void addListener (JPFListener l) {
    if (l instanceof VMListener) {
      if (vm != null) {
        vm.addListener( (VMListener) l);
      }
    }
    if (l instanceof SearchListener) {
      if (search != null) {
        search.addListener( (SearchListener) l);
      }
    }
  }

  public void addUniqueTypeListener (JPFListener l) {
    Class<?> cls = l.getClass();
    
    if (l instanceof VMListener) {
      if (vm != null) {
        if (!vm.hasListenerOfType(cls)) {
          vm.addListener( (VMListener) l);
        }
      }
    }
    if (l instanceof SearchListener) {
      if (search != null) {
        if (!search.hasListenerOfType(cls)) {
          search.addListener( (SearchListener) l);
        }
      }
    }
    
  }
  
  public void removeListener (JPFListener l){
    if (l instanceof VMListener) {
      if (vm != null) {
        vm.removeListener( (VMListener) l);
      }
    }
    if (l instanceof SearchListener) {
      if (search != null) {
        search.removeListener( (SearchListener) l);
      }
    }
  }

  public void addVMListener (VMListener l) {
    if (vm != null) {
      vm.addListener(l);
    }
  }

  public void addSearchProperty (Property p) {
    if (search != null) {
      search.addProperty(p);
    }
  }
  
  void addListeners () {

    // first, our system listeners
    Class<?>[] argTypes = { Config.class, JPF.class };
    Object[] args = { config, this };

    reporter = config.getInstance("jpf.report.class", Reporter.class, argTypes, args);
    if (reporter != null){
      addListener(reporter);
    }

    // now everything that's user configured
    ObjArray<JPFListener> listeners =
      config.getInstances("listener", JPFListener.class, argTypes, args);

    if (listeners != null) {
      for (JPFListener l : listeners) {
        addListener(l);
      }
    }
  }

  public Reporter getReporter () {
    return reporter;
  }

  public <T extends Publisher> boolean addPublisherExtension (Class<T> pCls, PublisherExtension e) {
    if (reporter != null) {
      return reporter.addPublisherExtension(pCls, e);
    }
    return false;
  }

  public <T extends Publisher> void setPublisherTopics (Class<T> pCls,
                                                        int category, String[] topics) {
    if (reporter != null) {
      reporter.setPublisherTopics(pCls, category, topics);
    }
  }


  public Config getConfig() {
    return config;
  }

  /**
   * return the search object. This can be null if the initialization has failed
   */
  public Search getSearch() {
    return search;
  }

  /**
   * return the VM object. This can be null if the initialization has failed
   */
  public JVM getVM() {
    return vm;
  }

  public static void exit() {
    // Hmm, exception as non local return. But we might be called from a
    // context we don't want to kill
    throw new ExitException();
  }

  public boolean foundErrors() {
    return !(search.getErrors().isEmpty());
  }


  public static boolean isHelpRequest (String[] args) {
    if (args == null) return true;
    if (args.length == 0) return true;

    for (int i=0; i<args.length; i++) {
      if ("-help".equals(args[i])) {
        args[i] = null;
        return true;
      }
    }

    return false;
  }

  public static boolean isPrintConfigRequest(String[] args) {
    if (args == null) return false;

    for (int i=0; i<args.length; i++) {
      if ("-show".equals(args[i])) {
        args[i] = null;
        return true;
      }
    }
    return false;
  }

  /**
   * this assumes that we have checked and 'consumed' (nullified) all known
   * options, so we just have to check for any '-' option prior to the
   * target class name
   */
  static void checkUnknownArgs (String[] args) {
    for ( int i=0; i<args.length; i++) {
      if (args[i] != null) {
        if (args[i].charAt(0) == '-') {
          logger.warning("unknown command line option: " + args[i]);
        }
        else {
          // this is supposed to be the target class name - everything that follows
          // is supposed to be processed by the program under test
          break;
        }
      }
    }
  }

  public static void printBanner (Config config) {
    System.out.println("Java Pathfinder Model Checker v" +
                  config.getString("jpf.version", "4") +
                  " - (C) 1999-2008 RIACS/NASA Ames Research Center");
  }


  /**
   * find the value of an arg that is either specific as
   * "-key=value" or as "-key value". If not found, the supplied
   * defValue is returned
   */
  static String getArg(String[] args, String pattern, String defValue, boolean consume) {
    String s = defValue;

    if (args != null){
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];

        if (arg != null) {
          if (arg.matches(pattern)) {
            int idx=arg.indexOf('=');
            if (idx > 0) {
              s = arg.substring(idx+1);
              if (consume) {
                args[i]=null;
              }
            } else if (i < args.length-1) {
              s = args[i+1];
              if (consume) {
                args[i] = null;
                args[i+1] = null;
              }
            }
            break;
          }
        }
      }
    }
    
    return s;
  }

  /**
   * what property file to look for
   */
  static String getConfigFileName (String[] args) {
    if (args.length > 0) {
      // check if the last arg is a mode property file
      // if yes, it has to include a 'target' spec
      int idx = args.length-1;
      String lastArg = args[idx];
      if (lastArg.endsWith(".jpf") || lastArg.endsWith(".properties")) {
        if (lastArg.startsWith("-c")) {
          int i = lastArg.indexOf('=');
          if (i > 0) {
            lastArg = lastArg.substring(i+1);
          }
        }
        args[idx] = null;
        return lastArg;
      }
    }
    
    return getArg(args, "-c(onfig)?(=.+)?", "jpf.properties", true);
  }

  /**
   * return a Config object that holds the JPF options. This first
   * loads the properties from a (potentially configured) properties file, and
   * then overlays all command line arguments that are key/value pairs
   */
  public static Config createConfig (String[] args) {
    return new Config(args, JPF.class);
  }
  
  /**
   * runs the verification.
   */
  public void run() {
    Runtime rt = Runtime.getRuntime();

    // this might be executed consecutively, so notify everybody
    RunRegistry.getDefaultRegistry().reset();

    if (isRunnable()) {
      try {
        if (vm.initialize()) {
          status = Status.RUNNING;
          search.search();
        }
      } catch (ExitException ex) {
        logger.severe( "JPF terminated");
        
      } catch (JPFException jx) {
        logger.severe( "JPF exception, terminating: " + jx.getMessage());
        if (config.getBoolean("jpf.print_exception_stack")) {
          
          Throwable cause = jx.getCause();
          if (cause != null && cause != jx){
            cause.printStackTrace();
          } else {
            jx.printStackTrace();            
          }
        }
        // pass this on, caller has to handle
        throw jx;
        
      } catch (OutOfMemoryError oom) {
        
        // try to get memory back before we do anything that makes it worse
        // (note that we even try to avoid calls here, we are on thin ice)
        memoryReserve = null; // release something
        long m0 = rt.freeMemory();
        long d = 10000;
        
        // request GCs as long as the freeMemory increases between cycles
        while (true) {
          rt.gc();
          long m1 = rt.freeMemory();
          if ((m1 <= m0) || ((m1 - m0) < d)) {
            break;
          }
          m0 = m1;
        }
        
        logger.severe("JPF out of memory");
        // that's questionable, but we might want to see statistics / coverage
        reporter.searchFinished(search);
        
      // NOTE - this is not an exception firewall anymore
        
      } finally {
        status = Status.DONE;
      }
    }
  }
  
  public List<Error> getSearchErrors () {
    if (search != null) {
      return search.getErrors();
    }

    return null;
  }

  static void showUsage() {
    System.out
        .println("Usage: \"java [<vm-option>..] gov.nasa.jpf.JPF [<jpf-option>..] [<app> [<app-arg>..]]");
    System.out.println("  <jpf-option> : -c <config-file>  : name of config properties file (default \"jpf.properties\")");
    System.out.println("               | -help  : print usage information");
    System.out.println("               | -show  : print configuration dictionary contents");
    System.out.println("               | +<key>=<value>  : add or override key/value pair to config dictionary");
    System.out.println("  <app>        : application class or *.xml error trace file");
    System.out.println("  <app-arg>    : arguments passed into main(String[]) if application class");
  }


  public static void handleException(JPFException e) {
    logger.severe(e.getMessage());
    exit();
  }

  /**
   * private helper class for local termination of JPF (without killing the
   * whole Java process via System.exit).
   * While this is basically a bad non-local goto exception, it seems to be the
   * least of evils given the current JPF structure, and the need to terminate
   * w/o exiting the whole Java process. If we just do a System.exit(), we couldn't
   * use JPF in an embedded context
   */
  @SuppressWarnings("serial")
  public static class ExitException extends RuntimeException {
    boolean report = true;
    
    ExitException() {}
    
    ExitException (boolean report){
      this.report = report;
    }
    
    ExitException(String msg) {
      super(msg);
    }
    
    public boolean shouldReport() {
      return report;
    }
  }
}
