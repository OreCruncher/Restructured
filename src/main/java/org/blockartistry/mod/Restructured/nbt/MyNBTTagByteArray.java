package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class MyNBTTagByteArray extends MyNBTBase {
	private byte[] byteArray;

	MyNBTTagByteArray() {
	}

	public MyNBTTagByteArray(byte[] array) {
		this.byteArray = array;
	}

	@Override
	void writeStream(DataOutput stream) throws IOException {
		stream.writeInt(this.byteArray.length);
		stream.write(this.byteArray);
	}

	@Override
	void readStream(DataInput stream, int depth, MyNBTSizeTracker tracker) throws IOException {
		tracker.func_152450_a(32L);
		int j = stream.readInt();
		tracker.func_152450_a(8 * j);
		this.byteArray = new byte[j];
		stream.readFully(this.byteArray);
	}

	public byte getId() {
		return 7;
	}

	public String toString() {
		return "[" + this.byteArray.length + " bytes]";
	}

	public MyNBTBase copy() {
		byte[] abyte = new byte[this.byteArray.length];
		System.arraycopy(this.byteArray, 0, abyte, 0, this.byteArray.length);
		return MyNBTBase.class.cast(new MyNBTTagByteArray(abyte));
	}

	public boolean equals(Object p_equals_1_) {
		return super.equals(p_equals_1_) ? Arrays.equals(this.byteArray, ((MyNBTTagByteArray) p_equals_1_).byteArray)
				: false;
	}

	public int hashCode() {
		return super.hashCode() ^ Arrays.hashCode(this.byteArray);
	}

	public byte[] func_150292_c() {
		return this.byteArray;
	}
}
