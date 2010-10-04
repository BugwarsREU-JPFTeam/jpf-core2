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
package gov.nasa.jpf.jvm.bytecode;

public abstract class InstructionVisitorAdapter 
							implements InstructionVisitor {

	public void visit(Instruction ins) {}

	public void visit(AALOAD ins) {}

	public void visit(AASTORE ins) {}

	public void visit(ACONST_NULL ins) {}

	public void visit(ALOAD ins) {}

	public void visit(ANEWARRAY ins) {}

	public void visit(ARETURN ins) {}

	public void visit(ArrayInstruction ins) {}

	public void visit(ARRAYLENGTH ins) {}

	public void visit(ArrayLoadInstruction ins) {}

	public void visit(ArrayStoreInstruction ins) {}

	public void visit(ASTORE ins) {}

	public void visit(ATHROW ins) {}

	public void visit(BALOAD ins) {}

	public void visit(BASTORE ins) {}

	public void visit(BIPUSH ins) {}

	public void visit(CALOAD ins) {}

	public void visit(CASTORE ins) {}

	public void visit(CHECKCAST ins) {}

	public void visit(D2F ins) {}

	public void visit(D2I ins) {}

	public void visit(D2L ins) {}

	public void visit(DADD ins) {}

	public void visit(DALOAD ins) {}

	public void visit(DASTORE ins) {}

	public void visit(DCMPG ins) {}

	public void visit(DCMPL ins) {}

	public void visit(DCONST ins) {}

	public void visit(DDIV ins) {}

	public void visit(DLOAD ins) {}

	public void visit(DMUL ins) {}

	public void visit(DNEG ins) {}

	public void visit(DREM ins) {}

	public void visit(DRETURN ins) {}

	public void visit(DSTORE ins) {}

	public void visit(DSUB ins) {}

	public void visit(DUP_X1 ins) {}

	public void visit(DUP_X2 ins) {}

	public void visit(DUP ins) {}

	public void visit(DUP2_X1 ins) {}

	public void visit(DUP2_X2 ins) {}

	public void visit(DUP2 ins) {}

	public void visit(F2D ins) {}

	public void visit(F2I ins) {}

	public void visit(FADD ins) {}

	public void visit(FALOAD ins) {}

	public void visit(FASTORE ins) {}

	public void visit(FCMPL ins) {}

	public void visit(FCONST ins) {}

	public void visit(FDIV ins) {}

	public void visit(FieldInstruction ins) {}

	public void visit(FLOAD ins) {}

	public void visit(FMUL ins) {}

	public void visit(FNEG ins) {}

	public void visit(FREM ins) {}

	public void visit(FRETURN ins) {}

	public void visit(FSTORE ins) {}

	public void visit(FSUB ins) {}

	public void visit(GETFIELD ins) {}

	public void visit(GETSTATIC ins) {}

	public void visit(GOTO_W ins) {}

	public void visit(GOTO ins) {}

	public void visit(I2B ins) {}

	public void visit(I2C ins) {}

	public void visit(I2D ins) {}

	public void visit(I2F ins) {}

	public void visit(I2L ins) {}

	public void visit(I2S ins) {}

	public void visit(IADD ins) {}

	public void visit(IALOAD ins) {}

	public void visit(IAND ins) {}

	public void visit(IASTORE ins) {}

	public void visit(ICONST ins) {}

	public void visit(IDIV ins) {}

	public void visit(IF_ACMPEQ ins) {}

	public void visit(IF_ACMPNE ins) {}

	public void visit(IF_ICMPEQ ins) {}

	public void visit(IF_ICMPGE ins) {}

	public void visit(IF_ICMPGT ins) {}

	public void visit(IF_ICMPLE ins) {}

	public void visit(IF_ICMPLT ins) {}

	public void visit(IF_ICMPNE ins) {}

	public void visit(IFEQ ins) {}

	public void visit(IFGE ins) {}

	public void visit(IFGT ins) {}

	public void visit(IfInstruction ins) {}

	public void visit(IFLE ins) {}

	public void visit(IFLT ins) {}

	public void visit(IFNE ins) {}

	public void visit(IFNONNULL ins) {}

	public void visit(IFNULL ins) {}

	public void visit(IINC ins) {}

	public void visit(ILOAD ins) {}

	public void visit(IMUL ins) {}

	public void visit(INEG ins) {}

	public void visit(InstanceFieldInstruction ins) {}

	public void visit(InstanceInvocation ins){}
	
	public void visit(INSTANCEOF ins){}
	
	public void visit(INVOKECG ins){}
	
	public void visit(INVOKECLINIT ins){}
	
	public void visit(InvokeInstruction ins){}
	
	public void visit(INVOKEINTERFACE ins){}
	
	public void visit(INVOKESPECIAL ins){}
	
	public void visit(INVOKESTATIC ins){}
	
	public void visit(INVOKEVIRTUAL ins){}

  public void visit(INVOKENATIVE ins){}
	
	public void visit(IOR ins){}
	
	public void visit(IREM ins){}
	
	public void visit(IRETURN ins){}
	
	public void visit(ISHL ins){}
	
	public void visit(ISHR ins){}
	
	public void visit(ISTORE ins){}
	
	public void visit(ISUB ins){}
	
	public void visit(IUSHR ins){}
	
	public void visit(IXOR ins){}
	
	public void visit(JSR_W ins){}
	
	public void visit(JSR ins){}
	
	public void visit(L2D ins){}
	
	public void visit(L2F ins){}
	
	public void visit(L2I ins){}
	
	public void visit(LADD ins){}
	
	public void visit(LALOAD ins){}
	
	public void visit(LAND ins){}
	
	public void visit(LASTORE ins){}
	
	public void visit(LCMP ins){}
	
	public void visit(LCONST ins){}
	
	public void visit(LDC_W ins){}
	
	public void visit(LDC ins){}
	
	public void visit(LDC2_W ins){}
	
	public void visit(LDIV ins){}
	
	public void visit(LLOAD ins){}
	
	public void visit(LMUL ins){}
	
	public void visit(LNEG ins){}
	
	public void visit(LocalVariableInstruction ins){}
	
	public void visit(LockInstruction ins){}
	
	public void visit(LongArrayLoadInstruction ins){}
	
	public void visit(LongArrayStoreInstruction ins){}
	
	public void visit(LOOKUPSWITCH ins){}
	
	public void visit(LOR ins){}
	
	public void visit(LREM ins){}
	
	public void visit(LRETURN ins){}
	
	public void visit(LSHL ins){}
	
	public void visit(LSHR ins){}
	
	public void visit(LSTORE ins){}
	
	public void visit(LSUB ins){}
	
	public void visit(LUSHR ins){}
	
	public void visit(LXOR ins){}
	
	public void visit(MONITORENTER ins){}
	
	public void visit(MONITOREXIT ins){}
	
	public void visit(MULTIANEWARRAY ins){}
	
	public void visit(NEW ins){}
	
	public void visit(NEWARRAY ins){}
	
	public void visit(NOP ins){}
	
	public void visit(POP ins){}
	
	public void visit(POP2 ins){}
	
	public void visit(PUTFIELD ins){}
	
	public void visit(PUTSTATIC ins){}
	
	public void visit(RET ins){}
	
	public void visit(RETURN ins){}
	
	public void visit(ReturnInstruction ins){}
	
	public void visit(RUNSTART ins){}
	
	public void visit(SALOAD ins){}
	
	public void visit(SASTORE ins){}
	
	public void visit(SIPUSH ins){}
	
	public void visit(StaticFieldInstruction ins){}

	public void visit(SWAP ins){}
	
	public void visit(SwitchInstruction ins){}
	
	public void visit(TABLESWITCH ins){}
	
	public void visit(VirtualInvocation ins){}
	
	public void visit(WIDE ins){}
	
}