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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Replaces Vanilla's NBTTagCompound class. It improves on the class in the
 * following ways:
 * 
 * + Copy operations are sensitive to whether elements are immutable in order to
 * avoid unnecessary object duplication.
 * 
 * + Backing storage is a single object array that contains both key and values.
 * Reduces overhead of storage costs.
 * 
 * + Add operations append to the internal array. No attempt is made to reuse
 * empty gaps. Focus is on creation performance with the expectation that
 * minimal element removals will occur.
 */
public class MyNBTTagCompound extends MyNBTBase {

	// Default list size. Enough space to hold 16 tags.
	// The reason for the size is to accommodate chunk
	// serialization as to minimize reallocations.
	private final static int SIZE_INCREMENT = 32;

	// The entities inserted into the compound are placed
	// into the list in key/value order. Any key searches
	// on the array will have to check every other element
	// starting with index 0. This list is not ordered
	// so the search would be linear. The goal is to have
	// fast insertions with a relatively stable content.
	private Object[] list = new Object[SIZE_INCREMENT];
	private int newIndex = 0;
	private int tagCount = 0;

	private void ensureCapacity(final int newObjects) {
		final int targetLength = newIndex + (newObjects * 2);
		if (targetLength <= list.length)
			return;

		int newLength = list.length;
		while ((newLength += SIZE_INCREMENT) < targetLength)
			;

		final Object[] newArray = new Object[newLength];
		System.arraycopy(list, 0, newArray, 0, list.length);
		list = newArray;
	}

	private int findIndex(final String key) {
		if (key != null && tagCount > 0)
			for (int i = 0; i < newIndex; i += 2)
				if (list[i] != null && list[i].equals(key))
					return i;
		return -1;
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		if (tagCount > 0) {
			for (int i = 0; i < newIndex; i += 2) {
				final MyNBTBase nbt = (MyNBTBase) list[i + 1];
				if (nbt == null)
					continue;

				final String key = (String) list[i];
				if (key == null)
					continue;

				writeTag(key, nbt, stream);
			}
		}
		stream.writeByte(NBT.TAG_END);
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		complexityCheck(depth);

		newIndex = 0;
		tagCount = 0;

		byte type;
		while ((type = readByte(stream, tracker)) != NBT.TAG_END) {
			final String key = readString(stream, tracker);
			MyNBTSizeTracker.readUTF(tracker, key);
			final MyNBTBase nbtbase = readTag(type, key, stream, depth + 1, tracker);
			setTag(key, nbtbase);
		}
	}

	public Set<String> func_150296_c() {
		final Set<String> result = new HashSet<String>();
		if (tagCount > 0)
			for (int i = 0; i < newIndex; i += 2)
				if (list[i] != null && list[i + 1] != null)
					result.add((String) list[i]);
		return result;
	}

	public byte getId() {
		return NBT.TAG_COMPOUND;
	}

	public void setTag(final String key, final MyNBTBase value) {
		if (value == null) {
			removeTag(key);
		} else {
			int idx = findIndex(key);
			if (idx != -1) {
				list[++idx] = value;
			} else {
				ensureCapacity(1);
				list[newIndex++] = key;
				list[newIndex++] = value;
				tagCount++;
			}
		}
	}

	public void setByte(final String key, final byte value) {
		setTag(key, new MyNBTTagByte(value));
	}

	public void setShort(final String key, final short value) {
		setTag(key, new MyNBTTagShort(value));
	}

	public void setInteger(final String key, final int value) {
		setTag(key, new MyNBTTagInt(value));
	}

	public void setLong(final String key, final long value) {
		setTag(key, new MyNBTTagLong(value));
	}

	public void setFloat(final String key, final float value) {
		setTag(key, new MyNBTTagFloat(value));
	}

	public void setDouble(final String key, final double value) {
		setTag(key, new MyNBTTagDouble(value));
	}

	public void setString(final String key, final String value) {
		setTag(key, new MyNBTTagString(value));
	}

	public void setByteArray(final String key, final byte[] value) {
		setTag(key, new MyNBTTagByteArray(value));
	}

	public void setIntArray(final String key, final int[] value) {
		setTag(key, new MyNBTTagIntArray(value));
	}

	public void setBoolean(final String key, final boolean value) {
		setByte(key, (byte) (value ? 1 : 0));
	}

	@SuppressWarnings("unchecked")
	protected <T> T getTagValue(int index) {
		return index == -1 ? null : (T) list[++index];
	}

	public MyNBTBase getTag(final String key) {
		return this.<MyNBTBase> getTagValue(findIndex(key));
	}

	public byte func_150299_b(final String key) {
		final MyNBTBase nbt = getTag(key);
		return nbt != null ? nbt.getId() : NBT.TAG_END;
	}

	public boolean hasKey(final String key) {
		return findIndex(key) != -1;
	}

	public boolean hasKey(final String key, final int type) {
		return func_150299_b(key) == type;
	}

	public byte getByte(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : this.<MyNBTPrimitive> getTagValue(idx).func_150290_f();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public short getShort(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : this.<MyNBTPrimitive> getTagValue(idx).func_150289_e();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public int getInteger(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : this.<MyNBTPrimitive> getTagValue(idx).func_150287_d();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public long getLong(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : this.<MyNBTPrimitive> getTagValue(idx).func_150291_c();
		} catch (ClassCastException classcastexception) {
		}
		return 0L;
	}

	public float getFloat(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : this.<MyNBTPrimitive> getTagValue(idx).func_150288_h();
		} catch (ClassCastException classcastexception) {
		}
		return 0.0F;
	}

	public double getDouble(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : this.<MyNBTPrimitive> getTagValue(idx).func_150286_g();
		} catch (ClassCastException classcastexception) {
		}
		return 0.0D;
	}

	public String getString(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? "" : this.<MyNBTBase> getTagValue(idx).func_150285_a_();
		} catch (ClassCastException classcastexception) {
		}
		return "";
	}

	public byte[] getByteArray(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? new byte[0] : this.<MyNBTTagByteArray> getTagValue(idx).func_150292_c();
		} catch (ClassCastException classcastexception) {
			throw new ReportedException(createCrashReport(key, 7, classcastexception));
		}
	}

	public int[] getIntArray(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? new int[0] : this.<MyNBTTagIntArray> getTagValue(idx).func_150302_c();
		} catch (ClassCastException classcastexception) {
			throw new ReportedException(createCrashReport(key, 11, classcastexception));
		}
	}

	public MyNBTTagCompound getCompoundTag(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? new MyNBTTagCompound() : this.<MyNBTTagCompound> getTagValue(idx);
		} catch (ClassCastException classcastexception) {
			;
			throw new ReportedException(createCrashReport(key, 10, classcastexception));
		}
	}

	public MyNBTTagList getTagList(final String key, final int type) {
		try {

			final MyNBTBase tag = getTag(key);
			if (tag == null || tag.getId() != NBT.TAG_LIST)
				return new MyNBTTagList();

			final MyNBTTagList nbt = (MyNBTTagList) tag;
			return nbt.tagCount() > 0 && nbt.func_150303_d() != type ? new MyNBTTagList() : nbt;
		} catch (ClassCastException classcastexception) {
			throw new ReportedException(createCrashReport(key, 9, classcastexception));
		}
	}

	public boolean getBoolean(final String key) {
		return getByte(key) != 0;
	}

	public void removeTag(final String key) {
		int idx = findIndex(key);
		if (idx != -1) {
			list[++idx] = null;
			tagCount--;
		}
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder(128);
		builder.append("{");

		if (tagCount > 0)
			for (int i = 0; i < newIndex; i += 2)
				if (list[i] != null)
					builder.append(list[i]).append(':').append(list[i + 1]).append(',');

		return builder.append('}').toString();
	}

	public boolean hasNoTags() {
		return tagCount == 0;
	}

	private CrashReport createCrashReport(final String key, final int type, final ClassCastException exception) {
		CrashReport crashreport = CrashReport.makeCrashReport(exception, "Reading NBT data");
		CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
		crashreportcategory.addCrashSectionCallable("Tag type found", new Callable<String>() {
			public String call() {
				return MyNBTBase.NBTTypes[((MyNBTBase) MyNBTTagCompound.this.getTag(key)).getId()];
			}
		});
		crashreportcategory.addCrashSectionCallable("Tag type expected", new Callable<String>() {
			public String call() {
				return MyNBTBase.NBTTypes[type];
			}
		});
		crashreportcategory.addCrashSection("Tag name", key);
		return crashreport;
	}

	private MyNBTBase copyHelper(final MyNBTBase src) {

		if (src.isImmutable())
			return src;

		return src.copy();
	}

	public MyNBTBase copy() {

		final MyNBTTagCompound nbt = new MyNBTTagCompound();

		if (tagCount > 0) {
			nbt.ensureCapacity(tagCount);
			int idx = 0;
			for (int i = 0; i < newIndex; i += 2) {
				final MyNBTBase src = this.<MyNBTBase> getTagValue(i);
				if (src == null)
					continue;

				final String key = (String) list[i];
				if (key == null)
					continue;

				nbt.list[idx++] = key;
				nbt.list[idx++] = copyHelper(src);
			}

			nbt.tagCount = tagCount;
			nbt.newIndex = idx;
		}

		return nbt;
	}

	public boolean equals(final Object tag) {

		if (!super.equals(tag))
			return false;

		// Share the same list for some reason?
		final MyNBTTagCompound nbt = (MyNBTTagCompound) tag;
		if (this.list == nbt.list)
			return true;

		// Same number of tags?
		if (this.tagCount != nbt.tagCount)
			return false;

		// Empty?
		if (tagCount == 0)
			return true;

		// Iterate through the keys. If they have the same
		// tag keys and the values associated with those keys
		// are equal, then the compounds are equal.
		for (int i = 0; i < newIndex; i += 2) {
			final MyNBTBase t = nbt.getTag((String) list[i]);
			final MyNBTBase s = this.<MyNBTBase> getTagValue(i);

			// If they are the same object
			if (t == s)
				continue;

			// If either are null..
			if (t == null || s == null)
				return false;

			if (!s.equals(t))
				return false;
		}

		// Must be equivalent
		return true;
	}

	public int hashCode() {
		return super.hashCode() ^ list.hashCode();
	}

	private static void writeTag(final String key, final MyNBTBase nbtBase, final DataOutput stream)
			throws IOException {
		stream.writeByte(nbtBase.getId());
		if (nbtBase.getId() != NBT.TAG_END) {
			stream.writeUTF(key);
			nbtBase.writeStream(stream);
		}
	}

	private static byte readByte(final DataInput stream, final MyNBTSizeTracker tracker) throws IOException {
		tracker.func_152450_a(8L);
		return stream.readByte();
	}

	private static String readString(final DataInput stream, final MyNBTSizeTracker tracker) throws IOException {
		return stream.readUTF();
	}

	static MyNBTBase readTag(final byte type, final String key, final DataInput stream, final int depth,
			final MyNBTSizeTracker tracker) {
		tracker.func_152450_a(32L);
		final MyNBTBase nbtbase = NBTFactory.getTag(type);
		try {
			nbtbase.readStream(stream, depth, tracker);
			return nbtbase;
		} catch (IOException ioexception) {
			CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
			crashreportcategory.addCrashSection("Tag name", key);
			crashreportcategory.addCrashSection("Tag type", Byte.valueOf(type));
			throw new ReportedException(crashreport);
		}
	}
}
