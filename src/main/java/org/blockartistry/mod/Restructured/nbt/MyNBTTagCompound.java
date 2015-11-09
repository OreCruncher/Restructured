package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public class MyNBTTagCompound extends MyNBTBase {

	private Map<String, MyNBTBase> tagMap = new HashMap<String, MyNBTBase>();

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		for (final Entry<String, MyNBTBase> entry : tagMap.entrySet())
			writeTag(entry.getKey(), entry.getValue(), stream);
		stream.writeByte(0);
	}

	@Override
	void readStream(DataInput stream, int depth, MyNBTSizeTracker tracker) throws IOException {
		if (depth > 512) {
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		}
		this.tagMap.clear();
		byte type;
		while ((type = readByte(stream, tracker)) != 0) {
			String key = readString(stream, tracker);
			MyNBTSizeTracker.readUTF(tracker, key);
			MyNBTBase nbtbase = readTag(type, key, stream, depth + 1, tracker);
			this.tagMap.put(key, nbtbase);
		}
	}

	public Set<String> func_150296_c() {
		return this.tagMap.keySet();
	}

	public byte getId() {
		return 10;
	}

	public void setTag(String key, MyNBTBase value) {
		this.tagMap.put(key, value);
	}

	public void setByte(String key, byte value) {
		this.tagMap.put(key, new MyNBTTagByte(value));
	}

	public void setShort(String key, short value) {
		this.tagMap.put(key, new MyNBTTagShort(value));
	}

	public void setInteger(String key, int value) {
		this.tagMap.put(key, new MyNBTTagInt(value));
	}

	public void setLong(String key, long value) {
		this.tagMap.put(key, new MyNBTTagLong(value));
	}

	public void setFloat(String key, float value) {
		this.tagMap.put(key, new MyNBTTagFloat(value));
	}

	public void setDouble(String key, double value) {
		this.tagMap.put(key, new MyNBTTagDouble(value));
	}

	public void setString(String key, String value) {
		this.tagMap.put(key, new MyNBTTagString(value));
	}

	public void setByteArray(String key, byte[] value) {
		this.tagMap.put(key, new MyNBTTagByteArray(value));
	}

	public void setIntArray(String key, int[] value) {
		this.tagMap.put(key, new MyNBTTagIntArray(value));
	}

	public void setBoolean(String key, boolean value) {
		setByte(key, (byte) (value ? 1 : 0));
	}

	public MyNBTBase getTag(String key) {
		return this.tagMap.get(key);
	}

	public byte func_150299_b(String key) {
		MyNBTBase nbtbase = (MyNBTBase) this.tagMap.get(key);
		return nbtbase != null ? nbtbase.getId() : 0;
	}

	public boolean hasKey(String key) {
		return this.tagMap.containsKey(key);
	}

	public boolean hasKey(String key, int type) {
		byte b0 = func_150299_b(key);
		return b0 == type;
	}

	public byte getByte(String key) {
		try {
			return !this.tagMap.containsKey(key) ? 0 : ((MyNBTPrimitive) this.tagMap.get(key)).func_150290_f();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public short getShort(String key) {
		try {
			return !this.tagMap.containsKey(key) ? 0 : ((MyNBTPrimitive) this.tagMap.get(key)).func_150289_e();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public int getInteger(String key) {
		try {
			return !this.tagMap.containsKey(key) ? 0 : ((MyNBTPrimitive) this.tagMap.get(key)).func_150287_d();
		} catch (ClassCastException classcastexception) {
		}
		return 0;
	}

	public long getLong(String key) {
		try {
			return !this.tagMap.containsKey(key) ? 0L : ((MyNBTPrimitive) this.tagMap.get(key)).func_150291_c();
		} catch (ClassCastException classcastexception) {
		}
		return 0L;
	}

	public float getFloat(String key) {
		try {
			return !this.tagMap.containsKey(key) ? 0.0F : ((MyNBTPrimitive) this.tagMap.get(key)).func_150288_h();
		} catch (ClassCastException classcastexception) {
		}
		return 0.0F;
	}

	public double getDouble(String key) {
		try {
			return !this.tagMap.containsKey(key) ? 0.0D : ((MyNBTPrimitive) this.tagMap.get(key)).func_150286_g();
		} catch (ClassCastException classcastexception) {
		}
		return 0.0D;
	}

	public String getString(String key) {
		try {
			return !this.tagMap.containsKey(key) ? "" : ((MyNBTBase) this.tagMap.get(key)).func_150285_a_();
		} catch (ClassCastException classcastexception) {
		}
		return "";
	}

	public byte[] getByteArray(String key) {
		try {
			return !this.tagMap.containsKey(key) ? new byte[0]
					: ((MyNBTTagByteArray) this.tagMap.get(key)).func_150292_c();
		} catch (ClassCastException classcastexception) {
			;
			throw new ReportedException(createCrashReport(key, 7, classcastexception));
		}
	}

	public int[] getIntArray(String key) {
		try {
			return !this.tagMap.containsKey(key) ? new int[0]
					: ((MyNBTTagIntArray) this.tagMap.get(key)).func_150302_c();
		} catch (ClassCastException classcastexception) {
			;
			throw new ReportedException(createCrashReport(key, 11, classcastexception));
		}
	}

	public MyNBTTagCompound getCompoundTag(String key) {
		try {
			return !this.tagMap.containsKey(key) ? new MyNBTTagCompound() : (MyNBTTagCompound) this.tagMap.get(key);
		} catch (ClassCastException classcastexception) {
			;
			throw new ReportedException(createCrashReport(key, 10, classcastexception));
		}
	}

	public MyNBTTagList getTagList(String key, int type) {
		try {
			if (func_150299_b(key) != 9) {
				return new MyNBTTagList();
			}
			MyNBTTagList nbttaglist = (MyNBTTagList) this.tagMap.get(key);
			return (nbttaglist.tagCount() > 0) && (nbttaglist.func_150303_d() != type) ? new MyNBTTagList()
					: nbttaglist;
		} catch (ClassCastException classcastexception) {
			;
			throw new ReportedException(createCrashReport(key, 9, classcastexception));
		}
	}

	public boolean getBoolean(String key) {
		return getByte(key) != 0;
	}

	public void removeTag(String key) {
		this.tagMap.remove(key);
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder(128);
		builder.append("{");

		for (final Entry<String, MyNBTBase> entry : tagMap.entrySet())
			builder.append(entry.getKey()).append(':').append(entry.getValue()).append(',');

		return builder.append('}').toString();
	}

	public boolean hasNoTags() {
		return this.tagMap.isEmpty();
	}

	private CrashReport createCrashReport(final String key, final int type, ClassCastException exception) {
		CrashReport crashreport = CrashReport.makeCrashReport(exception, "Reading NBT data");
		CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
		crashreportcategory.addCrashSectionCallable("Tag type found", new Callable<String>() {
			public String call() {
				return MyNBTBase.NBTTypes[((MyNBTBase) MyNBTTagCompound.this.tagMap.get(key)).getId()];
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

	public MyNBTBase copy() {

		MyNBTTagCompound nbt = new MyNBTTagCompound();
		for (final Entry<String, MyNBTBase> entry : tagMap.entrySet())
			nbt.setTag(entry.getKey(), entry.getValue().copy());

		return MyNBTBase.class.cast(nbt);
	}

	public boolean equals(Object p_equals_1_) {
		if (super.equals(p_equals_1_)) {
			MyNBTTagCompound nbttagcompound = (MyNBTTagCompound) p_equals_1_;
			return this.tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ this.tagMap.hashCode();
	}

	private static void writeTag(String key, MyNBTBase nbtBase, DataOutput stream) throws IOException {
		stream.writeByte(nbtBase.getId());
		if (nbtBase.getId() != 0) {
			stream.writeUTF(key);
			nbtBase.writeStream(stream);
		}
	}

	private static byte readByte(DataInput stream, MyNBTSizeTracker tracker) throws IOException {
		tracker.func_152450_a(8L);
		return stream.readByte();
	}

	private static String readString(DataInput stream, MyNBTSizeTracker tracker) throws IOException {
		return stream.readUTF();
	}

	static MyNBTBase readTag(byte type, String key, DataInput stream, int depth, MyNBTSizeTracker tracker) {
		tracker.func_152450_a(32L);
		MyNBTBase nbtbase = NBTFactory.getTag(type);
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
