package org.blockartistry.mod.Restructured.nbt;

public class MyNBTSizeTracker {
	
	public static final MyNBTSizeTracker noopTracker = new MyNBTSizeTracker(true);
	
	private final boolean noop;
	private final long field_152452_b;
	private long field_152453_c;

	private MyNBTSizeTracker(boolean noop) {
		this.noop = noop;
		this.field_152452_b = 0;
	}
	
	public MyNBTSizeTracker(long p_i1203_1_) {
		this.noop = false;
		this.field_152452_b = p_i1203_1_;
	}

	public void func_152450_a(long p_152450_1_) {
		if(noop)
			return;
		
		this.field_152453_c += p_152450_1_ / 8L;
		if (this.field_152453_c > this.field_152452_b) {
			throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: "
					+ this.field_152453_c + "bytes where max allowed: " + this.field_152452_b);
		}
	}

	public static void readUTF(MyNBTSizeTracker tracker, String data) {
		tracker.func_152450_a(16L);
		if (data == null) {
			return;
		}
		int len = data.length();
		int utflen = 0;
		for (int i = 0; i < len; i++) {
			int c = data.charAt(i);
			if ((c >= 1) && (c <= 127)) {
				utflen++;
			} else if (c > 2047) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}
		tracker.func_152450_a(8 * utflen);
	}
}
