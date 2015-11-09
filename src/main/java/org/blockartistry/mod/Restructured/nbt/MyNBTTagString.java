package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MyNBTTagString extends MyNBTBase {
	private String data;

	public MyNBTTagString() {
		this.data = "";
	}

	public MyNBTTagString(String p_i1389_1_) {
		this.data = p_i1389_1_;
		if (p_i1389_1_ == null) {
			throw new IllegalArgumentException("Empty string not allowed");
		}
	}

	@Override
	void writeStream(DataOutput p_74734_1_) throws IOException {
		p_74734_1_.writeUTF(this.data);
	}

	@Override
	void readStream(DataInput p_152446_1_, int p_152446_2_, MyNBTSizeTracker p_152446_3_) throws IOException {
		this.data = p_152446_1_.readUTF();
		MyNBTSizeTracker.readUTF(p_152446_3_, this.data);
	}

	public byte getId() {
		return 8;
	}

	public String toString() {
		return "\"" + this.data + "\"";
	}

	public MyNBTBase copy() {
		return MyNBTBase.class.cast(new MyNBTTagString(this.data));
	}

	public boolean equals(Object p_equals_1_) {
		;
		;
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
