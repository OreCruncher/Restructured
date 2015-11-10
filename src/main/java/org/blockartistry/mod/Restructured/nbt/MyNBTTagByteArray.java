package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import net.minecraftforge.common.util.Constants.NBT;

public class MyNBTTagByteArray extends MyNBTBase {

	private byte[] byteArray;

	MyNBTTagByteArray() {
	}

	public MyNBTTagByteArray(final byte[] array) {
		byteArray = array;
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		stream.writeInt(this.byteArray.length);
		stream.write(this.byteArray);
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		tracker.func_152450_a(32L);
		int j = stream.readInt();
		tracker.func_152450_a(8 * j);
		this.byteArray = new byte[j];
		stream.readFully(this.byteArray);
	}

	public byte getId() {
		return NBT.TAG_BYTE_ARRAY;
	}

	public String toString() {
		return "[" + this.byteArray.length + " bytes]";
	}

	public MyNBTBase copy() {
		final byte[] newArray = new byte[byteArray.length];
		System.arraycopy(byteArray, 0, newArray, 0, byteArray.length);
		return new MyNBTTagByteArray(newArray);
	}

	public boolean equals(final Object tag) {
		return super.equals(tag) && Arrays.equals(this.byteArray, ((MyNBTTagByteArray) tag).byteArray);
	}

	public int hashCode() {
		return super.hashCode() ^ Arrays.hashCode(this.byteArray);
	}

	public byte[] func_150292_c() {
		return this.byteArray;
	}
}
