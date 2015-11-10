package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraftforge.common.util.Constants.NBT;

public class MyNBTTagShort extends MyNBTPrimitive {
	private short data;

	public MyNBTTagShort() {
	}

	public MyNBTTagShort(final short value) {
		this.data = value;
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		stream.writeShort(this.data);
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		tracker.func_152450_a(16L);
		this.data = stream.readShort();
	}

	public byte getId() {
		return NBT.TAG_SHORT;
	}

	public String toString() {
		return "" + this.data + "s";
	}

	public MyNBTBase copy() {
		return new MyNBTTagShort(this.data);
	}

	public boolean equals(final Object p_equals_1_) {
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
