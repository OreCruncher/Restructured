package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyNBTTagList extends MyNBTBase {
	
	List<MyNBTBase> tagList = new ArrayList<MyNBTBase>();
	byte tagType = 0;

	@Override
	void writeStream(DataOutput stream) throws IOException {
		if (!this.tagList.isEmpty()) {
			this.tagType = ((MyNBTBase) this.tagList.get(0)).getId();
		} else {
			this.tagType = 0;
		}
		stream.writeByte(this.tagType);
		stream.writeInt(this.tagList.size());
		for(final MyNBTBase nbt : tagList)
			nbt.writeStream(stream);
	}

	@Override
	void readStream(DataInput stream, int depth, MyNBTSizeTracker tracker) throws IOException {
		if (depth > 512) {
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		}
		tracker.func_152450_a(8L);
		this.tagType = stream.readByte();
		tracker.func_152450_a(32L);
		int j = stream.readInt();
		this.tagList = new ArrayList<MyNBTBase>();
		for (int k = 0; k < j; k++) {
			tracker.func_152450_a(32L);
			MyNBTBase nbtbase = NBTFactory.getTag(this.tagType);
			nbtbase.readStream(stream, depth + 1, tracker);
			this.tagList.add(nbtbase);
		}
	}

	public byte getId() {
		return 9;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder(128);
		builder.append('[');
		
		int i = 0;
		for (Iterator<MyNBTBase> iterator = this.tagList.iterator(); iterator.hasNext(); i++) {
			final MyNBTBase nbtbase = iterator.next();
			builder.append(i).append(':').append(nbtbase.toString()).append(',');
		}
		return builder.append(']').toString();
	}

	public void appendTag(MyNBTBase tag) {

		if (this.tagType == 0) {
			this.tagType = tag.getId();
		} else if (this.tagType != tag.getId()) {
			System.err.println("WARNING: Adding mismatching tag types to tag list");
			return;
		}
		this.tagList.add(tag);
	}

	public void func_150304_a(int index, MyNBTBase tag) {

		if ((index >= 0) && (index < this.tagList.size())) {
			if (this.tagType == 0) {
				this.tagType = tag.getId();
			} else if (this.tagType != tag.getId()) {
				System.err.println("WARNING: Adding mismatching tag types to tag list");
				return;
			}
			this.tagList.set(index, tag);
		} else {
			System.err.println("WARNING: index out of bounds to set tag in tag list");
		}
	}

	public MyNBTBase removeTag(int index) {
		return this.tagList.remove(index);
	}

	public MyNBTTagCompound getCompoundTagAt(int index) {
		if ((index >= 0) && (index < this.tagList.size())) {
			MyNBTBase nbtbase = (MyNBTBase) this.tagList.get(index);
			return nbtbase.getId() == 10 ? (MyNBTTagCompound) nbtbase : new MyNBTTagCompound();
		}
		return new MyNBTTagCompound();
	}

	public int[] func_150306_c(int index) {
		if ((index >= 0) && (index < this.tagList.size())) {
			MyNBTBase nbtbase = (MyNBTBase) this.tagList.get(index);
			return nbtbase.getId() == 11 ? ((MyNBTTagIntArray) nbtbase).func_150302_c() : new int[0];
		}
		return new int[0];
	}

	public double func_150309_d(int index) {
		if ((index >= 0) && (index < this.tagList.size())) {
			MyNBTBase nbtbase = (MyNBTBase) this.tagList.get(index);
			return nbtbase.getId() == 6 ? ((MyNBTTagDouble) nbtbase).func_150286_g() : 0.0D;
		}
		return 0.0D;
	}

	public float func_150308_e(int index) {
		if ((index >= 0) && (index < this.tagList.size())) {
			MyNBTBase nbtbase = (MyNBTBase) this.tagList.get(index);
			return nbtbase.getId() == 5 ? ((MyNBTTagFloat) nbtbase).func_150288_h() : 0.0F;
		}
		return 0.0F;
	}

	public String getStringTagAt(int index) {
		if ((index >= 0) && (index < this.tagList.size())) {
			MyNBTBase nbtbase = (MyNBTBase) this.tagList.get(index);
			return nbtbase.getId() == 8 ? nbtbase.func_150285_a_() : nbtbase.toString();
		}
		return "";
	}

	public int tagCount() {
		return this.tagList.size();
	}

	public MyNBTBase copy() {
		MyNBTTagList nbttaglist = new MyNBTTagList();
		nbttaglist.tagType = this.tagType;
		
		for(final MyNBTBase tag : tagList)
			nbttaglist.tagList.add(tag.copy());

		return MyNBTBase.class.cast(nbttaglist);
	}

	public boolean equals(Object tag) {

		if (super.equals(tag)) {
			MyNBTTagList nbttaglist = (MyNBTTagList) tag;
			if (this.tagType == nbttaglist.tagType) {
				return this.tagList.equals(nbttaglist.tagList);
			}
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ this.tagList.hashCode();
	}

	public int func_150303_d() {
		return this.tagType;
	}
	
	private class Traversal<T> implements Iterator<T> {

		private Iterator<MyNBTBase> itr;
		
		public Traversal() {
			this.itr = MyNBTTagList.this.tagList.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return itr.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			return (T)itr.next();
		}
	}
	
	public <T> Iterator<T> getIterator() {
		return new Traversal<T>();
	}
}
