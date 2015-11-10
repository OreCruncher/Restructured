package org.blockartistry.mod.Restructured.nbt;

/**
 * Improvements over the Vanilla NBTSizeTracker:
 * 
 * + The static readUTF() no longer performs calculations
 * if the size tracker is noop.  Prior it would do the
 * calculation and throw the results away.
 *
 */
public class MyNBTSizeTracker {
	
	public static class NoopNBTTracker extends MyNBTSizeTracker {

		protected NoopNBTTracker() {
			super(0);
		}
		
		public boolean isNoop() {
			return true;
		}
		
		public void func_152450_a(final long value) {
			
		}
	}
	
	public static final MyNBTSizeTracker noopTracker = new NoopNBTTracker();
	
	private final long field_152452_b;
	private long field_152453_c;

	public MyNBTSizeTracker(long p_i1203_1_) {
		this.field_152452_b = p_i1203_1_;
	}

	public boolean isNoop() {
		return false;
	}
	
	public void func_152450_a(long p_152450_1_) {
		this.field_152453_c += p_152450_1_ / 8L;
		if (this.field_152453_c > this.field_152452_b) {
			throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: "
					+ this.field_152453_c + "bytes where max allowed: " + this.field_152452_b);
		}
	}

	public static void readUTF(MyNBTSizeTracker tracker, String data) {
		
		if(tracker.isNoop())
			return;
		
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
