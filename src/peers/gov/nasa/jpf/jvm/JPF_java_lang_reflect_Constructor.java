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

import java.lang.reflect.Modifier;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.MethodInfoRegistry;
import gov.nasa.jpf.util.RunListener;
import gov.nasa.jpf.util.RunRegistry;

/**
 * native peer for rudimentary constructor reflection.
 * 
 * Unfortunately, this is quite redundant to the Method peer, but Constructor
 * is not a Method subclass, and hence we can't rely on it's initialization
 */
public class JPF_java_lang_reflect_Constructor extends NativePeer {
  
  static MethodInfoRegistry registry;
  
  public static boolean init (Config conf) {
    // this is an example of how to handle cross-initialization between
    // native peers - this might also get explicitly called by the java.lang.Class
    // peer, since it creates Constructor objects. Here we have to make sure
    // we only reset between JPF runs
    
    if (registry == null){
      registry = new MethodInfoRegistry();
      
      RunRegistry.getDefaultRegistry().addListener( new RunListener() {
        public void reset (RunRegistry reg){
          registry = null;
        }
      });
    }
    return true;
  }

  static int createConstructorObject (MJIEnv env, ClassInfo ciCtor, MethodInfo mi){
    // note - it is the callers responsibility to ensure Constructor is properly initialized    
    
    int regIdx = registry.registerMethodInfo(mi);
    int eidx = env.newObject(ciCtor);
    ElementInfo ei = env.getElementInfo(eidx);
    
    ei.setIntField("regIdx", regIdx);
    return eidx;
  }

  static MethodInfo getMethodInfo (MJIEnv env, int objRef){
    return registry.getMethodInfo(env,objRef, "regIdx");
  }
  
  public static int getName____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    MethodInfo mi = getMethodInfo(env, objRef);
    
    int nameRef = env.getReferenceField( objRef, "name");
    if (nameRef == -1) {
      nameRef = env.newString(mi.getName());
      env.setReferenceField(objRef, "name", nameRef);
    }
   
    return nameRef;
  }
  
  // <2do> .. and some more delegations to JPF_java_lang_Method

  
  public static int newInstance___3Ljava_lang_Object_2__Ljava_lang_Object_2 (MJIEnv env, int mthRef,
                                                                             int argsRef) {
    String directCallId = "JPF_java_lang_reflect_Constructor.newInstance";
    ThreadInfo ti = env.getThreadInfo();
    StackFrame frame = ti.getReturnedDirectCall();

    if (frame == null || frame.getMethodInfo().getName() != directCallId){ // first time
      MethodInfo mi = getMethodInfo(env,mthRef);
      ClassInfo ci = mi.getClassInfo();

       if (ci.isAbstract()){
        env.throwException("java.lang.InstantiationException");
        return MJIEnv.NULL;
      }
      
      if (ci.requiresClinitExecution(ti)) {
        // NOTE - this might cause cause another direct call for a <clinit>
        env.repeatInvocation();
        return MJIEnv.NULL;
      }
      
      int objRef = env.newObject(ci);
      MethodInfo stub = mi.createDirectCallStub( directCallId);

      frame = new DirectCallStackFrame(stub, stub.getMaxStack()+1, stub.getMaxLocals());
      frame.push(objRef, true);  // (1) we store the return object on the frame
      frame.dup();

      if (!JPF_java_lang_reflect_Method.pushUnboxedArguments(env, mi, frame, argsRef)) {
        return MJIEnv.NULL;
      }

      ti.pushFrame(frame);

      //env.repeatInvocation(); // we don't need this, direct calls don't advance their return frame
      return MJIEnv.NULL; // doesn't matter, we come back
      
    } else { // reflection call returned
      while (frame.getMethodInfo().getName() != directCallId){
        // frame was the [clinit] direct call
        frame = frame.getPrevious();
      }
      
      int objRef = frame.pop(); // that's the object ref we pushed in (1)
      return objRef;
    }
  }
    
  public static int getParameterTypes_____3Ljava_lang_Class_2 (MJIEnv env, int objRef){
    // kind of dangerous, but we don't refer to any fields and the underlying JPF construct
    // (MethodInfo) is the same, so we just delegate to avoid copying non-trivial code
    return JPF_java_lang_reflect_Method.getParameterTypes (env, getMethodInfo(env,objRef));
  }

  
  public static int getAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int objRef){
    // <2do> check if ctor annotations are inherited, which is a bit off
    return JPF_java_lang_reflect_Method.getAnnotations( env, getMethodInfo(env,objRef));
  }
  
  public static int getDeclaredAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int objRef){
    return JPF_java_lang_reflect_Method.getDeclaredAnnotations( env, getMethodInfo(env,objRef));
  }
  
  public static int getAnnotation__Ljava_lang_Class_2__Ljava_lang_annotation_Annotation_2 (MJIEnv env, int objRef, int annotationClsRef) {
    return JPF_java_lang_reflect_Method.getAnnotation( env, getMethodInfo(env,objRef), annotationClsRef);
  }
  
  public static int getParameterAnnotations_____3_3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int objRef){
    return JPF_java_lang_reflect_Method.getParameterAnnotations( env, getMethodInfo(env,objRef));
  }

  
  public static int getModifiers____I (MJIEnv env, int objRef){
    MethodInfo mi = getMethodInfo(env, objRef);
    return mi.getModifiers();
  }

  public static int getDeclaringClass____Ljava_lang_Class_2 (MJIEnv env, int objRef){
    MethodInfo mi = getMethodInfo(env, objRef);    
    ClassInfo ci = mi.getClassInfo();
    // can't get a Constructor object w/o having initialized it's declaring class first
    return ci.getClassObjectRef();
  }
  
  public static int toString____Ljava_lang_String_2 (MJIEnv env, int objRef){
    StringBuilder sb = new StringBuilder();
    
    MethodInfo mi = getMethodInfo(env, objRef);

    sb.append(Modifier.toString(mi.getModifiers()));
    sb.append(' ');

    sb.append(mi.getClassInfo().getName());
    sb.append('(');
    
    String[] at = mi.getArgumentTypeNames();
    for (int i=0; i<at.length; i++){
      if (i>0) sb.append(',');
      sb.append(at[i]);
    }
    
    sb.append(')');
    
    int sref = env.newString(sb.toString());
    return sref;
  }

  public static boolean equals__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int mthRef){
    ElementInfo ei = env.getElementInfo(mthRef);
    ClassInfo ci = ClassInfo.getResolvedClassInfo(JPF_java_lang_Class.CONSTRUCTOR_CLASSNAME);

    if (ei.getClassInfo() == ci){
      MethodInfo mi1 = getMethodInfo(env, objRef);
      MethodInfo mi2 = getMethodInfo(env, mthRef);
      if (mi1.getClassInfo() == mi2.getClassInfo()){
        if (mi1.getName().equals(mi2.getName())){
          if (mi1.getReturnType().equals(mi2.getReturnType())){
            byte[] params1 = mi1.getArgumentTypes();
            byte[] params2 = mi2.getArgumentTypes();
            if (params1.length == params2.length){
              for (int i = 0; i < params1.length; i++){
                if (params1[i] != params2[i])
                  return false;
              }
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public static int hashCode____I (MJIEnv env, int objRef){
    MethodInfo ctor = getMethodInfo(env, objRef);
    return ctor.getClassName().hashCode();
  }
}
