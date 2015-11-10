package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraftforge.common.util.Constants.NBT;

public class MyNBTTagEnd extends MyNBTBase {
	
	public static final MyNBTTagEnd END = new MyNBTTagEnd();

	@Override
	void readStream(final DataInput stream, final int depth, final MyNBTSizeTracker tracker) throws IOException {
	}

	@Override
	void writeStream(final DataOutput stream) throws IOException {
	}

	public byte getId() {
		return NBT.TAG_END;
	}

	public String toString() {
		return "END";
	}

	public MyNBTBase copy() {
		return END;
	}
}
