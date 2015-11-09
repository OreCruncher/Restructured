package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.MathHelper;

public class MyNBTTagFloat extends MyNBTPrimitive {
	private float data;

	MyNBTTagFloat() {
	}

	public MyNBTTagFloat(float p_i45131_1_) {
		this.data = p_i45131_1_;
	}

	@Override
	void writeStream(DataOutput p_write_1_) throws IOException {
		p_write_1_.writeFloat(this.data);
	}

	@Override
	void readStream(DataInput p_152446_1_, int p_152446_2_, MyNBTSizeTracker p_152446_3_) throws IOException {
		p_152446_3_.func_152450_a(32L);
		this.data = p_152446_1_.readFloat();
	}

	public byte getId() {
		return 5;
	}

	public String toString() {
		return "" + this.data + "f";
	}

	public MyNBTBase copy() {
		return MyNBTBase.class.cast(new MyNBTTagFloat(this.data));
	}

	public boolean equals(Object p_equals_1_) {
		if (super.equals(p_equals_1_)) {
			MyNBTTagFloat localNBTTagFloat = (MyNBTTagFloat) p_equals_1_;
			return this.data == localNBTTagFloat.data;
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ Float.floatToIntBits(this.data);
	}

	public long func_150291_c() {
		return (long) this.data;
	}

	public int func_150287_d() {
		return MathHelper.floor_float(this.data);
	}

	public short func_150289_e() {
		return (short) (MathHelper.floor_float(this.data) & 0xFFFF);
	}

	public byte func_150290_f() {
		return (byte) (MathHelper.floor_float(this.data) & 0xFF);
	}

	public double func_150286_g() {
		return this.data;
	}

	public float func_150288_h() {
		return this.data;
	}
}
