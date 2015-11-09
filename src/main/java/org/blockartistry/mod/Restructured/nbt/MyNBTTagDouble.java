package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.MathHelper;

public class MyNBTTagDouble extends MyNBTPrimitive {
	private double data;

	MyNBTTagDouble() {
	}

	public MyNBTTagDouble(double p_i45130_1_) {
		this.data = p_i45130_1_;
	}

	@Override
	void writeStream(DataOutput p_write_1_) throws IOException {
		p_write_1_.writeDouble(this.data);
	}

	@Override
	void readStream(DataInput p_152446_1_, int p_152446_2_, MyNBTSizeTracker p_152446_3_) throws IOException {
		p_152446_3_.func_152450_a(64L);
		this.data = p_152446_1_.readDouble();
	}

	public byte getId() {
		return 6;
	}

	public String toString() {
		return "" + this.data + "d";
	}

	public MyNBTBase copy() {
		return MyNBTBase.class.cast(new MyNBTTagDouble(this.data));
	}

	public boolean equals(Object p_equals_1_) {
		if (super.equals(p_equals_1_)) {
			MyNBTTagDouble localNBTTagDouble = (MyNBTTagDouble) p_equals_1_;
			return this.data == localNBTTagDouble.data;
		}
		return false;
	}

	public int hashCode() {
		long l = Double.doubleToLongBits(this.data);
		return super.hashCode() ^ (int) (l ^ l >>> 32);
	}

	public long func_150291_c() {
		return (long) Math.floor(this.data);
	}

	public int func_150287_d() {
		return MathHelper.floor_double(this.data);
	}

	public short func_150289_e() {
		return (short) (MathHelper.floor_double(this.data) & 0xFFFF);
	}

	public byte func_150290_f() {
		return (byte) (MathHelper.floor_double(this.data) & 0xFF);
	}

	public double func_150286_g() {
		return this.data;
	}

	public float func_150288_h() {
		return (float) this.data;
	}
}
