package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MyNBTTagShort extends MyNBTPrimitive {
	private short data;

	public MyNBTTagShort() {
	}

	public MyNBTTagShort(short p_i45135_1_) {
		this.data = p_i45135_1_;
	}

	@Override
	void writeStream(DataOutput p_write_1_) throws IOException {
		p_write_1_.writeShort(this.data);
	}

	@Override
	void readStream(DataInput p_152446_1_, int p_152446_2_, MyNBTSizeTracker p_152446_3_) throws IOException {
		p_152446_3_.func_152450_a(16L);
		this.data = p_152446_1_.readShort();
	}

	public byte getId() {
		return 2;
	}

	public String toString() {
		return "" + this.data + "s";
	}

	public MyNBTBase copy() {
		return MyNBTBase.class.cast(new MyNBTTagShort(this.data));
	}

	public boolean equals(Object p_equals_1_) {
		if (super.equals(p_equals_1_)) {
			MyNBTTagShort localNBTTagShort = (MyNBTTagShort) p_equals_1_;
			return this.data == localNBTTagShort.data;
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ this.data;
	}

	public long func_150291_c() {
		return this.data;
	}

	public int func_150287_d() {
		return this.data;
	}

	public short func_150289_e() {
		return this.data;
	}

	public byte func_150290_f() {
		return (byte) (this.data & 0xFF);
	}

	public double func_150286_g() {
		return this.data;
	}

	public float func_150288_h() {
		return this.data;
	}
}
