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
package java.nio;

public abstract class Buffer {
	protected int position;
	protected int capacity;
	protected int limit;

	public final int capacity() {
		return capacity;
	}

	public final Buffer position(int newPosition) {
		if ((newPosition < 0) || (newPosition > limit)) {
			throw new IllegalArgumentException(
					"Illegal buffer position exception: " + newPosition);
		}
		this.position = newPosition;
		return this;
	}

	public final int position() {
		return position;
	}

	public final int limit() {
		return this.limit;
	}

	public final Buffer limit(int newLimit) {
		if ((newLimit < 0) || (newLimit > capacity)) {
			throw new IllegalArgumentException(
					"Illegal buffer limit exception: " + newLimit);
		}
		this.limit = newLimit;
		return this;
	}

	public final Buffer clear() {
		position = 0;
		limit = capacity;
		return this;
	}

	public final Buffer flip() {
		limit = position;
		position = 0;
		return this;
	}

	public final Buffer rewind() {
		position = 0;
		return this;
	}

	public final int remaining() {
		return limit - position;
	}

	public final boolean hasRemaining() {
		return remaining() > 0;
	}

	public abstract boolean hasArray();

	public abstract Object array();
}
