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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * This class provides a reusable stream object for storing
 * chunk information.  It replaces the classic DataOutputStream
 * that is normally returned by RegionFile and improves on it
 * in the following ways:
 * 
 * + Object pool for reducing pressure on GC and improving
 * performance a tad.
 * 
 * + The deflation compression objects are reused rather than
 * reallocating from the heap.  Helps mitigate impacts of
 * memory allocation and GC when realistically they can be
 * long lived objects with tons of reuse.
 * 
 * + The deflation parameters have been adjusted to improve
 * performance with little more data size.
 *
 */
public class ChunkOutputStream extends DataOutputStream {

	private static final AtomicInteger streamNumber = new AtomicInteger();
	private final static ConcurrentLinkedQueue<ChunkOutputStream> freeOutputStreams = new ConcurrentLinkedQueue<ChunkOutputStream>();

	public static ChunkOutputStream getStream(final int chunkX, final int chunkZ, final MyRegionFile region) {
		ChunkOutputStream buffer = freeOutputStreams.poll();
		if(buffer == null)
			buffer = new ChunkOutputStream();
		
		return buffer.reset(chunkX, chunkZ, region);
	}

	// Use different compression parameters than Vanilla, which
	// uses the default settings.
	private final static int COMPRESSION_LEVEL = 4;
	private final static int COMPRESSION_STRATEGY = Deflater.FILTERED;
	private final static int COMPRESSION_BUFFER_SIZE = 4096;

	@SuppressWarnings("unused")
	private int myID = streamNumber.incrementAndGet();

	private MyChunkBuffer myChunkBuffer = new MyChunkBuffer(0, 0, null);
	private Deflater myDeflater = new Deflater(COMPRESSION_LEVEL);
	private DeflaterOutputStream myDeflaterOutput;

	private ChunkOutputStream() {
		// The protected stream member will be set further down
		super(null);

		// Setup our buffers and deflater.  These guys will be
		// reused over and over...
		this.myDeflater = new Deflater(COMPRESSION_LEVEL);
		this.myDeflater.setStrategy(COMPRESSION_STRATEGY);
		this.myChunkBuffer = new MyChunkBuffer(0, 0, null);
		this.myDeflaterOutput = new DeflaterOutputStream(myChunkBuffer, myDeflater,
				COMPRESSION_BUFFER_SIZE);
		
		// Set the stream!
		this.out = this.myDeflaterOutput;
	}
	
	@Override
	public void close() throws IOException {
		// We don't want to close out the deflation stream.  Make it
		// finish what it's doing, and then tell our chunk buffer to
		// close().  This will cause an underlying write to occur.
		// Once all that is done toss the ChunkOutputStream on the
		// free list so it can be reused.
		this.myDeflaterOutput.finish();
		this.myChunkBuffer.close();
		freeOutputStreams.add(this);
	}
	
	protected ChunkOutputStream reset(final int chunkX, final int chunkZ, final MyRegionFile region) {
		// Reset our ChunkBuffer and Deflater for new work.
		this.myChunkBuffer.reset(chunkX, chunkZ, region);
		this.myDeflater.reset();
		return this;
	}
	
}
