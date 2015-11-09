package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MyNBTTagLong extends MyNBTPrimitive {
	private long data;

	MyNBTTagLong() {
	}

	public MyNBTTagLong(long p_i45134_1_) {
		this.data = p_i45134_1_;
	}

	@Override
	void writeStream(DataOutput p_write_1_) throws IOException {
		p_write_1_.writeLong(this.data);
	}

	@Override
	void readStream(DataInput p_152446_1_, int p_152446_2_, MyNBTSizeTracker p_152446_3_) throws IOException {
		p_152446_3_.func_152450_a(64L);
		this.data = p_152446_1_.readLong();
	}

	public byte getId() {
		return 4;
	}

	public String toString() {
		return "" + this.data + "L";
	}

	public MyNBTBase copy() {
		return MyNBTBase.class.cast(new MyNBTTagLong(this.data));
	}

	public boolean equals(Object p_equals_1_) {
		if (super.equals(p_equals_1_)) {
			MyNBTTagLong localNBTTagLong = (MyNBTTagLong) p_equals_1_;
			return this.data == localNBTTagLong.data;
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ (int) (this.data ^ this.data >>> 32);
	}

	public long func_150291_c() {
		return this.data;
	}

	public int func_150287_d() {
		return (int) (this.data & 0xFFFFFFFF);
	}

	public short func_150289_e() {
		return (short) (int) (this.data & 0xFFFF);
	}

	public byte func_150290_f() {
		return (byte) (int) (this.data & 0xFF);
	}

	public double func_150286_g() {
		return this.data;
	}

	public float func_150288_h() {
		return (float) this.data;
	}
}
