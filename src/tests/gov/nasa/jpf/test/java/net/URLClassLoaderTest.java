//
// Copyright (C) 2012 United States Government as represented by the
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
package gov.nasa.jpf.test.java.net;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * test of java.lang.ClassLoader API
 */
public class URLClassLoaderTest extends TestJPF {

  public class TestClassLoader extends URLClassLoader {

    public TestClassLoader(URL[] urls) {
        super(urls);
    }

    public TestClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }
    
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public Class<?> getLoadedClass(String name) {
      return findLoadedClass(name);
    }

    public Class<?> delegateTofindSystemClass(String cname) throws ClassNotFoundException {
      return this.findSystemClass(cname);
    }

    protected Package[] getPackages() {
      return super.getPackages();
    }

    protected Package getPackage(String name) {
      return super.getPackage(name);
    }
  }

  @Test
  public void testConstructor_NullPointerException() {
    if (verifyUnhandledException("java.lang.NullPointerException")) {
      new URLClassLoader(null);
    }
  }

  @Test 
  public void testConstructorEmptyURLs () {
    if (verifyNoPropertyViolation()) {
      URLClassLoader cl = new URLClassLoader(new URL[0]);
      assertNotNull(cl.getParent());
      assertEquals(cl.getParent(), ClassLoader.getSystemClassLoader());
    }
  }

  @Test
  public void testConstructorParent() {
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[0];
      ClassLoader parent = new TestClassLoader(urls);
      URLClassLoader cl =  new URLClassLoader(urls, parent);

      assertNotNull(parent.getParent());
      assertEquals(parent.getParent(), ClassLoader.getSystemClassLoader());

      assertNotNull(cl.getParent());
      assertEquals(cl.getParent(), parent);
    }
  }

  @Test
  public void testLoadClass_NoClassDefFoundError() throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.NoClassDefFoundError")) {
      URL[] urls = new URL[0];
      URLClassLoader cl = new URLClassLoader(urls);
      cl.loadClass("java/lang/Class");
    }
  }

  @Test
  public void testLoadClass_ClassNotFoundException() throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      URL[] urls = new URL[0];
      URLClassLoader cl =  new URLClassLoader(urls);
      cl.loadClass("java.lang.Does_Not_Exist");
    }
  }

  @Test
  public void testLoadClass_ClassNotFoundException2() throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      URL[] urls = new URL[0];
      URLClassLoader cl =  new URLClassLoader(urls);
      cl.loadClass("java.lang.Class.class");
    }
  }

  @Test
  public void testSystemLoaderLoadClass() throws ClassNotFoundException {
   if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[0];
      ClassLoader systemCl = ClassLoader.getSystemClassLoader();
      ClassLoader parent = new TestClassLoader(urls);
      URLClassLoader cl =  new URLClassLoader(urls, parent);

      String cname = "java.lang.Class";
      Class<?> c1 = systemCl.loadClass(cname);
      Class<?> c2 = parent.loadClass(cname);
      Class<?> c3 = cl.loadClass(cname);

      assertSame(c1, c2);
      assertSame(c1, c3);
      // this test fails on the host JVM, cause java.lang.Class is loaded by
      // bootstrap classloader and therefore c1.getClassLoader() returns null,
      // but the test passes on JPF.
      assertSame(c1.getClassLoader(), systemCl);
    }
  }

  @Test
  public void testFindLoadedClass() throws ClassNotFoundException, MalformedURLException {
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[0];
      TestClassLoader ucl1 = new TestClassLoader(urls);
      TestClassLoader ucl2 = new TestClassLoader(urls, ucl1);

      String cname = "java.lang.Class";

      Class<?> c = ucl2.loadClass(cname);
      assertNotNull(c);
      assertEquals(c.getName(), cname);

      // systemClassLoader is going to be the defining classloader
      assertNull(ucl2.getLoadedClass(cname));
      assertNull(ucl1.getLoadedClass(cname));
    }
  }

  String user_dir = System.getProperty("user.dir");
  String pkg = "classloader_specific_tests";

  String originalPath = user_dir + "/build/tests/" + pkg;
  String tempPath = user_dir + "/build/" + pkg;

  String jarUrl = "jar:file:" + user_dir + "/build/" + pkg + ".jar!/";
  String dirUrl = "file:" + user_dir + "/build";

  /**
   * move the package, to avoid systemClassLoader loading its classes
   */
  protected void movePkgOut() {
    if(!TestJPF.isJPFRun()) {
      movePkg(originalPath, tempPath);
    }
  }

  /**
   * move the package back to its original place
   */
  protected void movePkgBack() {
    if(!TestJPF.isJPFRun()) {
      movePkg(tempPath, originalPath);
    }
  }

  protected void movePkg(String from, String to) {
    File file = new File(to);
    if(!file.exists()) {
      file = new File(from);
      assertTrue(file.renameTo(new File(to)));
    }
  }

  @Test
  public void testNonSystemLoaderLoadClass() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      // create a url from a dir
      URL[] urls = { new URL(dirUrl) };
      URLClassLoader cl =  new URLClassLoader(urls);

      String cname = pkg + ".Class1";
      Class<?> cls = cl.loadClass(cname);

      assertEquals(cls.getClassLoader(), cl);
      assertFalse(cls.getClassLoader() == ClassLoader.getSystemClassLoader());

      assertEquals(cls.getInterfaces().length, 2);
      for(Class<?>ifc: cls.getInterfaces()) {
        assertEquals(cls.getClassLoader(), ifc.getClassLoader());
      }

      // create a url from jar
      urls[0] = new URL(jarUrl);
      cl =  new URLClassLoader(urls);
      cls = cl.loadClass(cname);

      assertEquals(cls.getClassLoader(), cl);
      assertFalse(cls.getClassLoader() == ClassLoader.getSystemClassLoader());
      assertEquals(cls.getInterfaces().length, 2);
      for(Class<?>ifc: cls.getInterfaces()) {
        assertEquals(cls.getClassLoader(), ifc.getClassLoader());
      }
    }
    movePkgBack();
  }

  @Test
  public void testFindResource() throws MalformedURLException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      URLClassLoader cl =  new URLClassLoader(urls);

      String resClass1 = pkg + "/Class1.class";
      URL url = cl.findResource(resClass1);
      String expectedUrl = dirUrl + "/" + resClass1;
      assertEquals(url.toString(), expectedUrl);

      String resInterface1 = pkg + "/Interface1.class";
      url = cl.findResource(resInterface1);
      expectedUrl = dirUrl + "/" + resInterface1;
      assertEquals(url.toString(), expectedUrl);

      url = cl.findResource("non_existence_resource");
      assertNull(url);

      url = cl.findResource("java/lang/Class.class");
      assertNull(url);

      // create a url from jar
      urls[0] = new URL(jarUrl);
      cl =  new URLClassLoader(urls);
      url = cl.findResource(resClass1);
      expectedUrl = jarUrl + resClass1;
      assertEquals(url.toString(), expectedUrl);

      url = cl.findResource(resInterface1);
      expectedUrl = jarUrl + resInterface1;
      assertEquals(url.toString(), expectedUrl);

      url = cl.findResource("non_existence_resource");
      assertNull(url);

      url = cl.findResource("java/lang/Class.class");
      assertNull(url);
    }
    movePkgBack();
  }

  @Test
  public void testFindResources() throws IOException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl), new URL(jarUrl), new URL(jarUrl) };
      URLClassLoader cl =  new URLClassLoader(urls);
      String resource = pkg + "/Class1.class";
      Enumeration<URL> e = cl.findResources(resource);

      List<String> urlList = new ArrayList<String>();
      while(e.hasMoreElements()) {
        urlList.add(e.nextElement().toString());
      }

      assertTrue(urlList.contains(jarUrl + resource));
      assertTrue(urlList.contains(dirUrl + "/" + resource));

      // we added the same url path twice, but findResource return value should only 
      // include one entry for the same resource
      assertEquals(urlList.size(), 2);

      e = cl.findResources(null);
      assertNotNull(e);
      assertFalse(e.hasMoreElements());
    }
    movePkgBack();
  }

  @Test
  public void testGetURLs() throws MalformedURLException {
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[5];
      urls[0] = new URL("file://" + "/x/y/z/" );
      urls[1] = new URL("file://" + "/a/b/c/" );
      urls[2] = new URL("file://" + "/a/b/c/" );
      urls[3] = new URL(dirUrl);;
      urls[4] = new URL(jarUrl);

      URLClassLoader cl =  new URLClassLoader(urls);
      URL[] clUrls = cl.getURLs();

      assertEquals(clUrls.length, urls.length);
      for (int i=0; i<urls.length; i++) {
        assertEquals(clUrls[i], urls[i]);
      }
    }
  }

  @Test
  public void testNewInstance1() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[1];
      urls[0] = new URL(dirUrl);
      URLClassLoader cl =  URLClassLoader.newInstance(urls);
      Class<?> c = cl.loadClass(pkg + ".Class1");
      assertNotNull(c);
      assertSame(c.getClassLoader(), cl);
      URL resource = cl.getResource(pkg + "/Interface1.class");
      assertNotNull(resource);
    }
    movePkgBack();
  }

  @Test
  public void testNewInstance2() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[1];
      urls[0] = new URL(dirUrl);
      URLClassLoader parent =  URLClassLoader.newInstance(urls);
      URLClassLoader cl =  URLClassLoader.newInstance(urls, parent);
      assertSame(parent, cl.getParent());

      Class<?> c = cl.loadClass(pkg + ".Class1");
      assertNotNull(c);
      assertSame(c.getClassLoader(), parent);

      String resName = pkg + "/Interface1.class";
      URL resource = cl.getResource(resName);
      assertNotNull(resource);

      resource = cl.getParent().getResource(resName);
      assertNotNull(resource);
    }
    movePkgBack();
  }

  public class Standard extends URLClassLoader {
    public Standard (URL[] urls) {
      super(urls);
    }

    public Standard(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }
  }

  public class Costum extends URLClassLoader {
    public Costum (URL[] urls) {
      super(urls);
    }
    
    public Costum(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }
    
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      return super.findClass(name);
    }
    
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      return super.loadClass(name);
    }
  }
    
  @Test
  public void testClassResolution() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      // create a url from a dir
      URL[] urls = { new URL(dirUrl) };
      String cname = pkg + ".Class1";
      String objClass = "java.lang.Object";

      Standard cl1 = new Standard(new URL[0]);
      Standard cl2 = new Standard(urls, cl1);
      Standard cl3 =  new Standard(urls, cl2);

      Class<?> c = cl3.loadClass(cname);
      assertEquals(c.getClassLoader(), cl2);

      c = cl3.loadClass(objClass);
      assertEquals(c.getClassLoader(), ClassLoader.getSystemClassLoader());

      Costum cl4 = new Costum(urls, null);
      Standard cl5 = new Standard(urls, cl4);

      c = cl5.loadClass(cname);
      assertEquals(c.getClassLoader(), cl4);
      assertSame(c, cl4.loadClass(cname));

      c = cl5.loadClass(objClass);
      assertEquals(c.getClassLoader(), ClassLoader.getSystemClassLoader());
      assertSame(c, cl4.loadClass(objClass));

      cl4 = new Costum(urls, cl3);
      cl5 = new Standard(urls, cl4);

      c = cl5.loadClass(cname);
      assertEquals(c.getClassLoader(), cl2);
      assertSame(c, cl4.loadClass(cname));

      c = cl5.loadClass(objClass);
      assertEquals(c.getClassLoader(), ClassLoader.getSystemClassLoader());
      assertSame(c, cl4.loadClass(objClass));
    }
    movePkgBack();
  }

  @Test
  public void testFindSystemClass() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader loader = new TestClassLoader(urls);
      assertNotNull(loader.delegateTofindSystemClass("java.lang.Class"));

      String cname = pkg + ".Class1";
      assertNotNull(loader.loadClass(cname));

      try {
        loader.delegateTofindSystemClass(cname);
      } catch(ClassNotFoundException e) {
        
      }
    }
    movePkgBack();
  }

  @Test
  public void testFindSystemClass_ClassNotFoundException() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader cl = new TestClassLoader(urls);
      String cname = pkg + ".Class1";

      // this should fail, cause our SystemClassLoader cannot find a non-standard 
      // class that is not on the classpath
      cl.delegateTofindSystemClass(cname);
    }
    movePkgBack();
  }

  @Test
  public void testGetPackages() throws ClassNotFoundException, MalformedURLException {
    movePkgOut();
    if(verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader cl = new TestClassLoader(urls);
      Package[] pkgs = cl.getPackages();

      boolean java_lang = false;
      boolean classloader_specific_tests = false;
      for(int i=0; i<pkgs.length; i++) {
        if(pkgs[i].getName().equals("java.lang")) {
          java_lang = true;
        } else if(pkgs[i].getName().equals("classloader_specific_tests")) {
          classloader_specific_tests = true;
        }
      }
      assertTrue(java_lang && !classloader_specific_tests);

      String cname = pkg + ".Class1";
      cl.loadClass(cname);
      pkgs = cl.getPackages();
      for(int i=0; i<pkgs.length; i++) {
        if(pkgs[i].getName().equals("java.lang")) {
          java_lang = true;
        } else if(pkgs[i].getName().equals("classloader_specific_tests")) {
          classloader_specific_tests = true;
        }
      }
      assertTrue(java_lang && classloader_specific_tests);
    }
    movePkgBack();
  }

  @Test
  public void testGetPackage() throws ClassNotFoundException, MalformedURLException {
    movePkgOut();
    if(verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader cl = new TestClassLoader(urls);
      assertNotNull(cl.getPackage("java.lang"));
      assertNull(cl.getPackage("non_existing_package"));
      assertNull(cl.getPackage("classloader_specific_tests"));

      String cname = pkg + ".Class1";
      cl.loadClass(cname);
      assertNotNull(cl.getPackage("classloader_specific_tests"));
    }
    movePkgBack();
  }
}
