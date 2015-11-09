package org.blockartistry.mod.Restructured.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MyNBTTagEnd extends MyNBTBase {

	@Override
	void readStream(DataInput p_152446_1_, int p_152446_2_, MyNBTSizeTracker p_152446_3_) throws IOException {
	}

	@Override
	void writeStream(DataOutput p_write_1_) throws IOException {
	}

	public byte getId() {
		return 0;
	}

	public String toString() {
		return "END";
	}

	public MyNBTBase copy() {
		return MyNBTBase.class.cast(new MyNBTTagEnd());
	}
}
