package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import net.minecraftforge.common.util.Constants.NBT;

public class MyNBTTagIntArray extends MyNBTBase {

	private int[] intArray;

	MyNBTTagIntArray() {
	}

	public MyNBTTagIntArray(final int[] array) {
		intArray = array;
		// intArray = new int[array.length];
		// System.arraycopy(array, 0, intArray, 0, array.length);
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		stream.writeInt(this.intArray.length);
		for (int i = 0; i < this.intArray.length; i++) {
			stream.writeInt(this.intArray[i]);
		}
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		tracker.func_152450_a(32L);
		final int j = stream.readInt();
		tracker.func_152450_a(32 * j);
		this.intArray = new int[j];
		for (int k = 0; k < j; k++) {
			this.intArray[k] = stream.readInt();
		}
	}

	public byte getId() {
		return NBT.TAG_INT_ARRAY;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder(128);
		builder.append('[');

		for (int j = 0; j < intArray.length; j++)
			builder.append(intArray[j]).append(',');

		return builder.append(']').toString();
	}

	public MyNBTBase copy() {
		final int[] newArray = new int[intArray.length];
		System.arraycopy(intArray, 0, newArray, 0, intArray.length);
		return new MyNBTTagIntArray(newArray);
	}

	public boolean equals(Object tag) {
		return super.equals(tag) ? Arrays.equals(this.intArray, ((MyNBTTagIntArray) tag).intArray) : false;
	}

	public int hashCode() {
		return super.hashCode() ^ Arrays.hashCode(this.intArray);
	}

	public int[] func_150302_c() {
		return this.intArray;
	}
}
