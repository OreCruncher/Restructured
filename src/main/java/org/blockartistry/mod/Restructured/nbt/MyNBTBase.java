/* This file is part of Restructured, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Replaces Vanilla's NBTBase.  It improves upon NBTBase with the following:
 * 
 * + Immutable attribute - indicates what tags are immutable once initialized.
 * Helps minimizes impacts of doing copies since immutable objects can be shared
 * without issue.  Currently supported by the primitive types.
 * 
 * + NBTPrimitive move from being from an inner class to it's own class file.
 */
public abstract class MyNBTBase {

	public static final String[] NBTTypes = { "END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]",
			"STRING", "LIST", "COMPOUND", "INT[]" };

	abstract void writeStream(final DataOutput paramDataOutput) throws IOException;

	abstract void readStream(final DataInput stream, final int paramInt, final MyNBTSizeTracker tracker)
			throws IOException;

	public abstract String toString();

	public abstract byte getId();

	public abstract MyNBTBase copy();

	public boolean isImmutable() {
		return false;
	}

	public boolean equals(final Object tag) {
		return this == tag || (tag instanceof MyNBTBase && this.getId() == ((MyNBTBase) tag).getId());
	}

	public int hashCode() {
		return getId();
	}

	protected String func_150285_a_() {
		return toString();
	}
	
	protected static void complexityCheck(final int depth) {
		if (depth > 512) {
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		}
	}
}
