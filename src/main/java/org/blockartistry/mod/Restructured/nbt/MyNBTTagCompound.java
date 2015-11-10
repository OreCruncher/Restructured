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

public class MyNBTTagCompound extends MyNBTBase {

	private final static int DEFAULT_LIST_SIZE = 64;

	private Object[] list = new Object[DEFAULT_LIST_SIZE];
	private int newIndex = 0;
	private int tagCount = 0;

	private void ensureCapacity(final int size) {
		if (size <= list.length)
			return;

		int newLength = list.length;
		while (newLength < size)
			newLength = newLength << 1;

		final Object[] newArray = new Object[newLength];
		System.arraycopy(list, 0, newArray, 0, list.length);
		list = newArray;
	}

	private int findIndex(final String key) {
		if (tagCount > 0)
			for (int i = 0; i < newIndex; i += 2)
				if (list[i] != null && list[i].equals(key))
					return i;
		return -1;
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		if (tagCount > 0) {
			for (int i = 0; i < newIndex; i += 2)
				if (list[i] != null)
					writeTag((String) list[i], (MyNBTBase) list[i + 1], stream);
		}
		stream.writeByte(0);
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		if (depth > 512) {
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		}

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
				if (list[i] != null)
					result.add((String) list[i]);
		return result;
	}

	public byte getId() {
		return NBT.TAG_COMPOUND;
	}

	public void setTag(final String key, final MyNBTBase value) {
		int idx = findIndex(key);
		if (idx != -1) {
			list[++idx] = value;
		} else {
			ensureCapacity(newIndex + 2);
			list[newIndex++] = key;
			list[newIndex++] = value;
			tagCount++;
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

	public MyNBTBase getTag(final String key) {
		int idx = findIndex(key);
		return idx == -1 ? null : (MyNBTBase) list[++idx];
	}

	public byte func_150299_b(final String key) {
		MyNBTBase nbtbase = getTag(key);
		return nbtbase != null ? nbtbase.getId() : 0;
	}

	public boolean hasKey(final String key) {
		return findIndex(key) != -1;
	}

	public boolean hasKey(final String key, final int type) {
		byte b0 = func_150299_b(key);
		return b0 == type;
	}

	public byte getByte(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : ((MyNBTPrimitive) list[++idx]).func_150290_f();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public short getShort(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : ((MyNBTPrimitive) list[++idx]).func_150289_e();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public int getInteger(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : ((MyNBTPrimitive) list[++idx]).func_150287_d();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public long getLong(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : ((MyNBTPrimitive) list[++idx]).func_150291_c();
		} catch (ClassCastException classcastexception) {
		}
		return 0L;
	}

	public float getFloat(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : ((MyNBTPrimitive) list[++idx]).func_150288_h();
		} catch (ClassCastException classcastexception) {
		}
		return 0.0F;
	}

	public double getDouble(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? 0 : ((MyNBTPrimitive) list[++idx]).func_150286_g();
		} catch (ClassCastException classcastexception) {
		}
		return 0.0D;
	}

	public String getString(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? "" : ((MyNBTBase) list[++idx]).func_150285_a_();
		} catch (ClassCastException classcastexception) {
		}
		return "";
	}

	public byte[] getByteArray(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? new byte[0] : ((MyNBTTagByteArray) list[++idx]).func_150292_c();
		} catch (ClassCastException classcastexception) {
			throw new ReportedException(createCrashReport(key, 7, classcastexception));
		}
	}

	public int[] getIntArray(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? new int[0] : ((MyNBTTagIntArray) list[++idx]).func_150302_c();
		} catch (ClassCastException classcastexception) {
			throw new ReportedException(createCrashReport(key, 11, classcastexception));
		}
	}

	public MyNBTTagCompound getCompoundTag(final String key) {
		try {
			int idx = findIndex(key);
			return idx == -1 ? new MyNBTTagCompound() : ((MyNBTTagCompound) list[++idx]);
		} catch (ClassCastException classcastexception) {
			;
			throw new ReportedException(createCrashReport(key, 10, classcastexception));
		}
	}

	public MyNBTTagList getTagList(final String key, final int type) {
		try {
			if (func_150299_b(key) != 9) {
				return new MyNBTTagList();
			}
			MyNBTTagList nbttaglist = (MyNBTTagList) getTag(key);
			return (nbttaglist.tagCount() > 0) && (nbttaglist.func_150303_d() != type) ? new MyNBTTagList()
					: nbttaglist;
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
			list[idx++] = null;
			list[idx] = null;
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

	private MyNBTBase immutableCopy(final MyNBTBase src) {
		if(!src.isImmutable())
			src.copy();
		
		return src;
	}
	
	public MyNBTBase copy() {

		final MyNBTTagCompound nbt = new MyNBTTagCompound();

		if (tagCount > 0) {
			nbt.ensureCapacity(tagCount * 2);
			int idx = 0;
			for (int i = 0; i < newIndex; i += 2)
				if (list[i] != null) {
					nbt.list[idx++] = list[i];
					nbt.list[idx++] = immutableCopy((MyNBTBase)list[i + 1]);
				}
			nbt.tagCount = tagCount;
			nbt.newIndex = idx;
		}

		return nbt;
	}

	public boolean equals(final Object p_equals_1_) {
		if (super.equals(p_equals_1_)) {
			MyNBTTagCompound nbttagcompound = (MyNBTTagCompound) p_equals_1_;
			return list.equals(nbttagcompound.list);
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ list.hashCode();
	}

	private static void writeTag(final String key, final MyNBTBase nbtBase, final DataOutput stream)
			throws IOException {
		stream.writeByte(nbtBase.getId());
		if (nbtBase.getId() != 0) {
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
