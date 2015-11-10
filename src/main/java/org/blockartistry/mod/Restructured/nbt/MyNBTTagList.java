package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraftforge.common.util.Constants.NBT;

public class MyNBTTagList extends MyNBTBase {

	private static final int DEFAULT_SIZE = 16;

	// Key/Value stored consecutively in the list
	private MyNBTBase[] list = new MyNBTBase[DEFAULT_SIZE];
	private int newIndex = 0;
	private int tagCount = 0;
	byte tagType = NBT.TAG_END;

	@Override
	void writeStream(final DataOutput stream) throws IOException {

		if (newIndex == 0)
			tagType = NBT.TAG_END;
		else
			tagType = list[0].getId();

		stream.writeByte(tagType);
		stream.writeInt(newIndex);

		if (tagCount > 0)
			for (int i = 0; i < newIndex; i++)
				if (list[i] != null)
					list[i].writeStream(stream);
	}

	private void ensureCapacity(final int size) {
		if (size <= list.length)
			return;

		int newLength = list.length;
		while (newLength < size)
			newLength = newLength << 1;

		final MyNBTBase[] newArray = new MyNBTBase[newLength];
		System.arraycopy(list, 0, newArray, 0, list.length);
		list = newArray;
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		if (depth > 512) {
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		}
		tracker.func_152450_a(8L);
		this.tagType = stream.readByte();
		tracker.func_152450_a(32L);
		final int totalObjs = stream.readInt();
		ensureCapacity(totalObjs + 1);
		for (int i = 0; i < totalObjs; i++) {
			tracker.func_152450_a(32L);
			MyNBTBase nbtbase = NBTFactory.getTag(this.tagType);
			nbtbase.readStream(stream, depth + 1, tracker);
			appendTag(nbtbase);
		}
	}

	public byte getId() {
		return NBT.TAG_LIST;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder(128);
		builder.append('[');

		for (int i = 0; i < newIndex; i++)
			if (list[i] != null)
				builder.append(i).append(':').append(list[i].toString()).append(',');

		return builder.append(']').toString();
	}

	public void appendTag(final MyNBTBase tag) {
		if (tagType == NBT.TAG_END) {
			tagType = tag.getId();
		} else if (tagType != tag.getId()) {
			System.err.println("WARNING: Adding mismatching tag types to tag list");
			return;
		}

		ensureCapacity(newIndex + 1);
		list[newIndex++] = tag;
		tagCount++;
	}

	public void func_150304_a(final int index, final MyNBTBase tag) {

		if ((index >= 0) && (index < newIndex)) {
			if (tagType == NBT.TAG_END) {
				tagType = tag.getId();
			} else if (tagType != tag.getId()) {
				System.err.println("WARNING: Adding mismatching tag types to tag list");
				return;
			}
			if (list[index] == null)
				tagCount++;
			list[index] = tag;
		} else {
			System.err.println("WARNING: index out of bounds to set tag in tag list");
		}
	}

	public MyNBTBase removeTag(final int index) {
		if (list[index] != null)
			tagCount--;
		return list[index] = null;
	}

	public MyNBTTagCompound getCompoundTagAt(final int index) {
		if ((index >= 0) && (index < newIndex)) {
			MyNBTBase nbtbase = list[index];
			return nbtbase.getId() == NBT.TAG_COMPOUND ? (MyNBTTagCompound) nbtbase : new MyNBTTagCompound();
		}
		return new MyNBTTagCompound();
	}

	public int[] func_150306_c(final int index) {
		if ((index >= 0) && (index < newIndex)) {
			MyNBTBase nbtbase = list[index];
			return nbtbase.getId() == NBT.TAG_INT_ARRAY ? ((MyNBTTagIntArray) nbtbase).func_150302_c() : new int[0];
		}
		return new int[0];
	}

	public double func_150309_d(final int index) {
		if ((index >= 0) && (index < newIndex)) {
			MyNBTBase nbtbase = list[index];
			return nbtbase.getId() == NBT.TAG_DOUBLE ? ((MyNBTTagDouble) nbtbase).func_150286_g() : 0.0D;
		}
		return 0.0D;
	}

	public float func_150308_e(final int index) {
		if ((index >= 0) && (index < newIndex)) {
			MyNBTBase nbtbase = list[index];
			return nbtbase.getId() == NBT.TAG_FLOAT ? ((MyNBTTagFloat) nbtbase).func_150288_h() : 0.0F;
		}
		return 0.0F;
	}

	public String getStringTagAt(final int index) {
		if ((index >= 0) && (index < newIndex)) {
			MyNBTBase nbtbase = list[index];
			return nbtbase.getId() == NBT.TAG_STRING ? nbtbase.func_150285_a_() : nbtbase.toString();
		}
		return "";
	}

	public int tagCount() {
		return newIndex;
	}

	public MyNBTBase copy() {

		MyNBTTagList nbttaglist = new MyNBTTagList();
		nbttaglist.tagType = this.tagType;

		if (newIndex > 0) {
			nbttaglist.ensureCapacity(newIndex);
			nbttaglist.tagCount = tagCount;
			if (NBTFactory.isImmutable(tagType)) {
				System.arraycopy(list, 0, nbttaglist.list, 0, list.length);
				nbttaglist.newIndex = newIndex;
			} else {
				int idx = 0;
				for (int i = 0; i < newIndex; i++)
					if (list[i] != null)
						nbttaglist.list[idx++] = list[i].copy();
				nbttaglist.newIndex = idx;
			}
		}

		return nbttaglist;
	}

	public boolean equals(final Object tag) {

		if (super.equals(tag)) {
			MyNBTTagList nbttaglist = (MyNBTTagList) tag;
			if (this.tagType == nbttaglist.tagType) {
				return list.equals(nbttaglist.list);
			}
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ list.hashCode();
	}

	public int func_150303_d() {
		return this.tagType;
	}
}
