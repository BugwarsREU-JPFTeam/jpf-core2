//
// Copyright (C) 2010 United States Government as represented by the
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

package gov.nasa.jpf.util;

import gov.nasa.jpf.JPFException;


/**
 * a fixed size BitSet with 256 bits.
 *
 * The main motivation for this class is to minimize memory size while maximizing
 * performance and keeping a java.util.BitSet compatible interface. The only
 * deviation from the standard BitSet is that we assume more cardinality() calls
 * than set()/clear() calls, i.e. we want to cache this value
 *
 * Instances of this class do not allocate any additional memory, we keep all
 * data in builtin type fields
 */
public class BitSet256 implements FixedBitSet, Cloneable {

  public static final int INDEX_MASK = 0xffffff00;

  long l0, l1, l2, l3;
  int cardinality;

  public BitSet256 (){
    // nothing in here
  }

  public BitSet256 (int i){
    set(i);
  }

  public BitSet256 (int... idx){
    for (int i : idx){
      set(i);
    }
  }

  public int longSize(){
    return  4;
  }
  public long getLong(int i){
    switch (i){
      case 0: return l0;
      case 1: return l1;
      case 2: return l2;
      case 3: return l3;
      default:
        throw new IndexOutOfBoundsException("BitSet64 has no long index " + i);
    }
  }

  public BitSet256 clone() {
    try {
      return (BitSet256) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new JPFException("BitSet256 clone failed");
    }
  }

  private final int computeCardinality (){
    int n= Long.bitCount(l0);
    n += Long.bitCount(l1);
    n += Long.bitCount(l2);
    n += Long.bitCount(l3);
    return n;
  }

  //--- public interface (much like java.util.BitSet)

  public void set (int i){
    if ((i & INDEX_MASK) == 0) {
      long bitPattern = (1L << i);

      switch (i >> 6) {
        case 0:
          if ((l0 & bitPattern) == 0L) {
            cardinality++;
            l0 |= bitPattern;
          }
          break;
        case 1:
          if ((l1 & bitPattern) == 0L) {
            cardinality++;
            l1 |= bitPattern;
          }
          break;
        case 2:
          if ((l2 & bitPattern) == 0L) {
            cardinality++;
            l2 |= bitPattern;
          }
          break;
        case 3:
          if ((l3 & bitPattern) == 0L) {
            cardinality++;
            l3 |= bitPattern;
          }
      }
    } else {
      throw new IndexOutOfBoundsException("BitSet256 index out of range: " + i);
    }
  }

  public void clear (int i){
    if ((i & INDEX_MASK) == 0) {
      long bitPattern = ~(1L << i);

      switch (i >> 6) {
        case 0:
          if ((l0 & bitPattern) != 0L) {
            cardinality--;
            l0 &= bitPattern;
          }
          break;
        case 1:
          if ((l1 & bitPattern) != 0L) {
            cardinality--;
            l1 &= bitPattern;
          }
          break;
        case 2:
          if ((l2 & bitPattern) != 0L) {
            cardinality--;
            l2 &= bitPattern;
          }
          break;
        case 3:
          if ((l3 & bitPattern) != 0L) {
            cardinality--;
            l3 &= bitPattern;
          }
      }
    } else {
      throw new IndexOutOfBoundsException("BitSet256 index out of range: " + i);
    }
  }

  public void set (int i, boolean val){
    if (val) {
      set(i);
    } else {
      clear(i);
    }
  }

  public boolean get (int i){
    if ((i & INDEX_MASK) == 0) {
      long bitPattern = (1L << i);

      switch (i >> 6) {
        case 0:
          return ((l0 & bitPattern) != 0);
        case 1:
          return ((l1 & bitPattern) != 0);
        case 2:
          return ((l2 & bitPattern) != 0);
        case 3:
          return ((l3 & bitPattern) != 0);
      }
    }

    throw new IndexOutOfBoundsException("BitSet256 index out of range: " + i);
  }

  public int cardinality() {
    return cardinality;
  }

  /**
   * number of bits we can store
   */
  public int size() {
    return 256;
  }

  /**
   * index of highest set bit + 1
   */
  public int length() {
    if (l3 != 0){
      return 256 - Long.numberOfLeadingZeros(l3);
    } else if (l2 != 0){
      return 192 - Long.numberOfLeadingZeros(l2);
    } else if (l1 != 0){
      return 128 - Long.numberOfLeadingZeros(l1);
    } else if (l1 != 0){
      return 64 - Long.numberOfLeadingZeros(l0);
    } else {
      return 0;
    }
  }

  public boolean isEmpty() {
    return (cardinality == 0);
  }

  public void clear() {
    l0 = l1 = l2 = l3 = 0L;
    cardinality = 0;
  }


  public int nextSetBit (int fromIdx){
    if ((fromIdx & INDEX_MASK) == 0) {
      int i;
      int i0 = fromIdx & 0x3f;
      switch (fromIdx >> 6){
        case 0:
          if ((i=Long.numberOfTrailingZeros(l0 & (0xffffffffffffffffL << i0))) <64) return i;
          if ((i=Long.numberOfTrailingZeros(l1)) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(l3)) <64) return i + 192;
          break;
        case 1:
          if ((i=Long.numberOfTrailingZeros(l1 & (0xffffffffffffffffL << i0))) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(l3)) <64) return i + 192;
          break;
        case 2:
          if ((i=Long.numberOfTrailingZeros(l2 & (0xffffffffffffffffL << i0))) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(l3)) <64) return i + 192;
          break;
        case 3:
          if ((i=Long.numberOfTrailingZeros(l3 & (0xffffffffffffffffL << i0))) <64) return i + 192;
      }

      return -1;

    } else {
      throw new IndexOutOfBoundsException("BitSet256 index out of range: " + fromIdx);
    }
  }

  public int nextClearBit (int fromIdx){
    if ((fromIdx & INDEX_MASK) == 0) {
      int i;
      int i0 = fromIdx & 0x3f;
      switch (fromIdx >> 6){
        case 0:
          if ((i=Long.numberOfTrailingZeros(~l0 & (0xffffffffffffffffL << i0))) <64) return i;
          if ((i=Long.numberOfTrailingZeros(~l1)) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(~l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(~l3)) <64) return i + 192;
          break;
        case 1:
          if ((i=Long.numberOfTrailingZeros(~l1 & (0xffffffffffffffffL << i0))) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(~l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(~l3)) <64) return i + 192;
          break;
        case 2:
          if ((i=Long.numberOfTrailingZeros(~l2 & (0xffffffffffffffffL << i0))) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(~l3)) <64) return i + 192;
          break;
        case 3:
          if ((i=Long.numberOfTrailingZeros(~l3 & (0xffffffffffffffffL << i0))) <64) return i + 192;
      }

      return -1;

    } else {
      throw new IndexOutOfBoundsException("BitSet256 index out of range: " + fromIdx);
    }
  }

  public void and (BitSet256 other){
    l0 &= other.l0;
    l1 &= other.l1;
    l2 &= other.l2;
    l3 &= other.l3;

    cardinality = computeCardinality();
  }

  public void andNot (BitSet256 other){
    l0 &= ~other.l0;
    l1 &= ~other.l1;
    l2 &= ~other.l2;
    l3 &= ~other.l3;

    cardinality = computeCardinality();
  }

  public void or (BitSet256 other){
    l0 |= other.l0;
    l1 |= other.l1;
    l2 |= other.l2;
    l3 |= other.l3;

    cardinality = computeCardinality();
  }

  public boolean equals (Object o){
    if (o instanceof BitSet256){
      BitSet256 other = (BitSet256)o;
      if (l0 != other.l0) return false;
      if (l1 != other.l1) return false;
      if (l2 != other.l2) return false;
      if (l3 != other.l3) return false;
      return true;
    } else {
      // <2do> we could compare to a normal java.util.BitSet here
      return false;
    }
  }

  public void hash(HashData hd){
    hd.add(hashCode());
  }

  /**
   * answer the same hashCodes as java.util.BitSet
   */
  public int hashCode() {
    long hc = 1234;
    hc ^= l0;
    hc ^= l1*2;
    hc ^= l2*3;
    hc ^= l3*4;
    return (int) ((hc >>32) ^ hc);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');

    boolean first = true;
    for (int i=nextSetBit(0); i>= 0; i = nextSetBit(i+1)){
      if (!first){
        sb.append(',');
      } else {
        first = false;
      }
      sb.append(i);
    }

    sb.append('}');

    return sb.toString();
  }
}
