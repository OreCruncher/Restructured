/* This file is part of Restructured, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.Restructured.chunk;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Replaces Minecraft MyChunkBuffer. It improves on Minecraft's implementation
 * in the following ways:
 * 
 * + Default allocation is 8 sectors. Most newly formed/empty chunks take one or
 * two sectors to hold NBT information.  It is expected that modded chunks may
 * take more sectors.
 * 
 * + The chunk stream header is incorporated into the buffer to reduce impact on
 * the underlying write routines.
 * 
 * + Not based on ByteArrayOutputStream; removed synchronized methods because
 * the buffer is only access by a single thread during writes.
 */
public class MyChunkBuffer extends OutputStream {

	// Time measurement stuff. Intended to work with
	// concurrent ChunkBuffer writes in the case of
	// multiple IO write threads.
	private final static boolean DO_TIMINGS = false;
	private final static AtomicInteger bytesWritten;
	private final static AtomicInteger totalWrites;
	private final static AtomicInteger outstandingWrites;
	private final static AtomicInteger accumulatedTime;
	private static volatile long timeMarker = 0;

	static {
		if (DO_TIMINGS) {
			bytesWritten = new AtomicInteger();
			totalWrites = new AtomicInteger();
			outstandingWrites = new AtomicInteger();
			accumulatedTime = new AtomicInteger();
		} else {
			bytesWritten = null;
			totalWrites = null;
			outstandingWrites = null;
			accumulatedTime = null;
		}
	}

	private final static int DEFAULT_BUFFER_SIZE = 4096 * 8;
	private final static int CHUNK_HEADER_SIZE = 5;

	private MyRegionFile file;
	private int chunkX;
	private int chunkZ;
	private byte[] buf;
	private int count;

	public MyChunkBuffer(final int x, final int z, final MyRegionFile file) {
		this.file = file;
		this.chunkX = x;
		this.chunkZ = z;

		this.buf = new byte[DEFAULT_BUFFER_SIZE];
	}

	public void reset() {
		// Leave space for the header
		count = CHUNK_HEADER_SIZE;

		// Getting the timeMaker initialized isn't
		// 100% perfect because of potential thread
		// switch but it is good enough.
		if (DO_TIMINGS)
			if (outstandingWrites.incrementAndGet() == 1)
				timeMarker = System.nanoTime();
	}

	public int size() {
		// The header is silent
		return count - CHUNK_HEADER_SIZE;
	}

	private void ensureCapacity(int minCapacity) {
		// overflow-conscious code
		if (minCapacity - buf.length > 0)
			grow(minCapacity);
	}

	private void grow(int minCapacity) {
		// overflow-conscious code
		int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
		buf = Arrays.copyOf(buf, newCapacity);
	}

	public void write(int b) {
		ensureCapacity(count + 1);
		buf[count] = (byte) b;
		count += 1;
	}

	public void write(byte b[], int off, int len) {
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) - b.length > 0)) {
			throw new IndexOutOfBoundsException();
		}
		ensureCapacity(count + len);
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	public void close() throws IOException {

		// Fill out the header before
		// passing off. It's BigEndian, and
		// includes the byte for compression
		// type - not sure why.
		final int len = count - CHUNK_HEADER_SIZE + 1;
		buf[0] = (byte) ((len >>> 24) & 0xFF);
		buf[1] = (byte) ((len >>> 16) & 0xFF);
		buf[2] = (byte) ((len >>> 8) & 0xFF);
		buf[3] = (byte) ((len >>> 0) & 0xFF);

		// Set our stream version - currently
		// Inflate/Deflate.
		buf[4] = 2; // STREAM_VERSION_FLATION

		// Do the write.  This is a blocking call if other
		// ChunkBuffers are writing to the *same* region
		// file at the same time.
		file.write(chunkX, chunkZ, buf, count);
		file = null;

		if (DO_TIMINGS) {
			final int totalBytes = bytesWritten.addAndGet(count);
			final int numWrites = totalWrites.incrementAndGet();

			// If it was the last one doing a write dump out
			// some stats to the console.  This is not 100%
			// perfect because a thread switch could occur
			// between the outstandingWrites adjustment
			// and the accumulatedTime modification, and the
			// resulting data printed to the console could be
			// off.  However, if it is the last write the
			// information printed will be correct.
			if (outstandingWrites.decrementAndGet() == 0) {
				final int accumTime = accumulatedTime.addAndGet((int) ((System.nanoTime() - timeMarker) / 1000));

				final float avgWriteTime = accumTime / numWrites;
				final float avgBytes = totalBytes / numWrites;
				final float throughput = avgBytes / avgWriteTime;
				System.out.println(String.format("MyChunkBuffer time %f, size %f (rate %f b/msec)", avgWriteTime,
						avgBytes, throughput));
			}
		}
	}

	public MyChunkBuffer reset(final int chunkX, final int chunkZ, final MyRegionFile file) {
		this.file = file;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.reset();
		return this;
	}
}
