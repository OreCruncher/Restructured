package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraftforge.common.util.Constants.NBT;

public class MyNBTTagString extends MyNBTBase {
	private String data;

	public MyNBTTagString() {
		this.data = "";
	}

	public MyNBTTagString(final String text) {
		this.data = text;
		if (text == null) {
			throw new IllegalArgumentException("Empty string not allowed");
		}
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		stream.writeUTF(this.data);
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		this.data = stream.readUTF();
		MyNBTSizeTracker.readUTF(tracker, this.data);
	}

	public byte getId() {
		return NBT.TAG_STRING;
	}

	public String toString() {
		return "\"" + this.data + "\"";
	}

	public MyNBTBase copy() {
		return new MyNBTTagString(this.data);
	}

	public boolean equals(final Object p_equals_1_) {
		if (!super.equals(p_equals_1_)) {
			return false;
		}
		MyNBTTagString nbttagstring = (MyNBTTagString) p_equals_1_;
		return ((this.data == null) && (nbttagstring.data == null))
				|| ((this.data != null) && (this.data.equals(nbttagstring.data)));
	}

	public int hashCode() {
		return super.hashCode() ^ this.data.hashCode();
	}

	public String func_150285_a_() {
		return this.data;
	}
}
