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
package gov.nasa.jpf.jvm;

import java.util.Map;

import gov.nasa.jpf.classfile.ClassPath;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * Native peer for java.lang.ClassLoader
 */
public class JPF_java_lang_ClassLoader {

  public static void $init____V (MJIEnv env, int objRef) {
    ClassLoaderInfo systemCl = ClassLoaderInfo.getCurrentSystemClassLoader();
    $init__Ljava_lang_ClassLoader_2__V (env, objRef, systemCl.getClassLoaderObjectRef());
  }

  public static void $init__Ljava_lang_ClassLoader_2__V (MJIEnv env, int objRef, int parentRef) {
    Heap heap = env.getHeap();

    //--- Retrieve the parent ClassLoaderInfo
    ClassLoaderInfo parent = env.getClassLoaderInfo(parentRef);

    //--- create the internal representation of the classloader
    ClassLoaderInfo cl = new ClassLoaderInfo(env.getVM(), objRef, new ClassPath(), parent);

    //--- initialize the java.lang.ClassLoader object
    ElementInfo ei = heap.get(objRef);
    ei.setIntField("clRef", cl.getGlobalId());

    // we should use the following block if we ever decide to make systemClassLoader 
    // unavailable if(parent.isSystemClassLoader) {
    //  // we don't want to make the systemCLassLoader available through SUT
    //  parentRef = MJIEnv.NULL;
    // }

    ei.setReferenceField("parent", parentRef);
  }

  public static int getSystemClassLoader____Ljava_lang_ClassLoader_2 (MJIEnv env, int clsObjRef) {
    return ClassLoaderInfo.getCurrentSystemClassLoader().getClassLoaderObjectRef();
  }

  public static int getResource0__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int resRef){
    String rname = env.getStringObject(resRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    String resourcePath = cl.findResource(rname);

    return env.newString(resourcePath);
  }

  public static int getResources0__Ljava_lang_String_2___3Ljava_lang_String_2 (MJIEnv env, int objRef, int resRef) {
    String rname = env.getStringObject(resRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    String[] resources = cl.findResources(rname);

    return env.newStringArray(resources);
  }

  public static int findLoadedClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env, int objRef, int nameRef) {
    String cname = env.getStringObject(nameRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    ClassInfo ci = cl.getAlreadyResolvedClassInfo(cname);
    if(ci != null) {
      return ci.getClassObjectRef();
    }

    return MJIEnv.NULL;
  }

  public static int findSystemClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env, int objRef, int nameRef) {
    String cname = env.getStringObject(nameRef);

    checkForIllegalName(env, cname);
    if(env.hasException()) {
      return MJIEnv.NULL;
    }

    ClassLoaderInfo cl = ClassLoaderInfo.getCurrentSystemClassLoader();

    ClassInfo ci = cl.getResolvedClassInfo(cname);

    if(!ci.isRegistered()) {
      ci.registerClass(env.getThreadInfo());
    }

    return ci.getClassObjectRef();
  }

  public static int defineClass0__Ljava_lang_String_2_3BII__Ljava_lang_Class_2 
           (MJIEnv env, int objRef, int nameRef, int bufferRef, int offset, int length) {
    // retrieve ClassLoaderInfo instance
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    String cname = env.getStringObject(nameRef);
    byte[] buffer = env.getByteArrayObject(bufferRef);

    return defineClass(env, cl, cname, buffer, offset, length, null);
  }
  
  protected static int defineClass (MJIEnv env, ClassLoaderInfo cl, String cname, byte[] buffer, int offset, int length, ClassPath.Match match) {

    if(!check(env, cname, buffer, offset, length)) {
      return MJIEnv.NULL;
    }

    if(match == null) {
      match = cl.getMatch(cname);
    }

    // determine whether that the corresponding class is already defined by this 
    // classloader, if so, this attempt is invalid, and loading throws a LinkageError
    if(cl.getDefinedClassInfo(cname) != null) {
      env.throwException("java.lang.LinkageError");
      return MJIEnv.NULL;
    }

    ClassInfo ci = cl.getResolvedClassInfo(cname, buffer, offset, length, match);

    // Note: if the representation is not of a supported major or minor version, loading 
    // throws an UnsupportedClassVersionError. But for now, we do not check for this here 
    // since we don't do much with minor and major versions

    ThreadInfo ti = env.getThreadInfo();
    ci.registerClass(ti);

    return ci.getClassObjectRef();
  }

  protected static boolean check(MJIEnv env, String cname, byte[] buffer, int offset, int length) {
    // throw SecurityExcpetion if the package prefix is java
    checkForProhibitedPkg(env, cname);

    // throw NoClassDefFoundError if the given class does name might 
    // not be a valid binary name
    checkForIllegalName(env, cname);

    // throw IndexOutOfBoundsException if buffer length is not consistent
    // with offset
    checkData(env, buffer, offset, length);

    return !env.hasException();
  }

  protected static void checkForProhibitedPkg(MJIEnv env, String name) {
    if(name != null && name.startsWith("java.")) {
      env.throwException("java.lang.SecurityException", "Prohibited package name: " + name);
    }
  }

  protected static void checkForIllegalName(MJIEnv env, String name) {
    if((name == null) || (name.length() == 0)) {
      return;
    }

    if((name.indexOf('/') != -1) || (name.charAt(0) == '[')) {
      env.throwException("java.lang.NoClassDefFoundError", "IllegalName: " + name);
    }
  }

  protected static void checkData(MJIEnv env, byte[] buffer, int offset, int length) {
    if(offset<0 || length<0 || offset+length > buffer.length) {
      env.throwException("java.lang.IndexOutOfBoundsException");
    }
  }

  static String pkg_class_name = "java.lang.Package";

  public static int getPackages_____3Ljava_lang_Package_2 (MJIEnv env, int objRef) {
    ClassLoaderInfo sysLoader = ClassLoaderInfo.getCurrentSystemClassLoader();
    ClassInfo pkgClass = null; 
    try {
      pkgClass = sysLoader.getInitializedClassInfo(pkg_class_name, env.getThreadInfo());
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    // Returns all of the Packages defined by this class loader and its ancestors
    Map<String, ClassLoaderInfo> pkgs = cl.getPackages();
    int size = pkgs.size();
    // create an array of type java.lang.Package
    int pkgArr = env.newObjectArray(pkg_class_name, size);

    int i = 0;
    for(String name: cl.getPackages().keySet()) {
      int pkgRef = createPackageObject(env, pkgClass, name, cl);
      // place the object into the array
      env.setReferenceArrayElement(pkgArr, i++, pkgRef);
    }

    return pkgArr;
  }

  public static int getPackage__Ljava_lang_String_2__Ljava_lang_Package_2 (MJIEnv env, int objRef, int nameRef) {
    ClassLoaderInfo sysLoader = ClassLoaderInfo.getCurrentSystemClassLoader();

    ClassInfo pkgClass = null; 
    try {
      pkgClass = sysLoader.getInitializedClassInfo(pkg_class_name, env.getThreadInfo());
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    String pkgName = env.getStringObject(nameRef);
    if(cl.getPackages().get(pkgName)!=null) {
      return createPackageObject(env, pkgClass, pkgName, cl);
    } else {
      return MJIEnv.NULL;
    }
  }

  public static int createPackageObject(MJIEnv env, ClassInfo pkgClass, String pkgName, ClassLoaderInfo cl) {
    int pkgRef = env.newObject(pkgClass);
    ElementInfo ei = env.getElementInfo(pkgRef);

    ei.setReferenceField("pkgName", env.newString(pkgName));
    ei.setReferenceField("loader", cl.getClassLoaderObjectRef());
    // the rest of the fields set to some dummy value
    ei.setReferenceField("specTitle", env.newString("spectitle"));
    ei.setReferenceField("specVersion", env.newString("specversion"));
    ei.setReferenceField("specVendor", env.newString("specvendor"));
    ei.setReferenceField("implTitle", env.newString("impltitle"));
    ei.setReferenceField("implVersion", env.newString("implversion"));
    ei.setReferenceField("sealBase", MJIEnv.NULL);

    return pkgRef;
  }
}
