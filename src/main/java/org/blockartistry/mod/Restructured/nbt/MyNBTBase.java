package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class MyNBTBase {

	public static final String[] NBTTypes = { "END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]",
			"STRING", "LIST", "COMPOUND", "INT[]" };

	abstract void writeStream(final DataOutput paramDataOutput) throws IOException;

	abstract void readStream(final DataInput stream, final int paramInt,
			final MyNBTSizeTracker tracker) throws IOException;

	public abstract String toString();

	public abstract byte getId();

	public abstract MyNBTBase copy();

	public boolean isImmutable() {
		return NBTFactory.isImmutable(getId());
	}
	
	public boolean equals(Object p_equals_1_) {
		if (!(p_equals_1_ instanceof MyNBTBase)) {
			return false;
		}
		MyNBTBase localNBTBase = (MyNBTBase) p_equals_1_;
		if (getId() != localNBTBase.getId()) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		return getId();
	}

	protected String func_150285_a_() {
		return toString();
	}
}
