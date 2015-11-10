package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraftforge.common.util.Constants.NBT;

public class MyNBTTagLong extends MyNBTPrimitive {
	private long data;

	MyNBTTagLong() {
	}

	public MyNBTTagLong(final long value) {
		this.data = value;
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
		stream.writeLong(this.data);
	}

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
		tracker.func_152450_a(64L);
		this.data = stream.readLong();
	}

	public byte getId() {
		return NBT.TAG_LONG;
	}

	public String toString() {
		return "" + this.data + "L";
	}

	public MyNBTBase copy() {
		return new MyNBTTagLong(this.data);
	}

	public boolean equals(final Object tag) {
		return super.equals(tag) && this.data == ((MyNBTTagLong) tag).data;
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
