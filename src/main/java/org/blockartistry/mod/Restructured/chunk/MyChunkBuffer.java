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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Replaces Minecraft MyChunkBuffer.  It improves on Minecraft's implementation
 * in the following ways:
 * 
 * + Default allocation is 4 sectors.  Most newly formed/empty chunks take
 * one or two sectors to hold NBT information.
 * 
 * + Used ChunkBuffers are placed on a queue for re-use to mitigate allocation
 * overhead and reduce pressure for the GC.
 * 
 * + The chunk stream header is incorporated into the buffer to reduce impact
 * on the underlying write routines.
 * 
 * + Not based on ByteArrayOutputStream; removed synchronized methods because
 * the buffer is only access by a single thread during writes.
 */
public class MyChunkBuffer extends OutputStream {

	private final static boolean DO_TIMINGS = false;
	
	private final static int DEFAULT_BUFFER_SIZE = 4096 * 4;
	private final static int CHUNK_HEADER_SIZE = 5;
	private final static ConcurrentLinkedQueue<MyChunkBuffer> freeChunkBuffers = new ConcurrentLinkedQueue<MyChunkBuffer>();

	public static MyChunkBuffer getChunkBuffer(final int chunkX, final int chunkZ, final MyRegionFile region) {
		MyChunkBuffer buffer = freeChunkBuffers.poll();
		return (buffer != null) ? buffer.reset(chunkX, chunkZ, region) : new MyChunkBuffer(chunkX, chunkZ, region);
	}

	private MyRegionFile file;
	private int chunkX;
	private int chunkZ;
	private byte[] buf;
	private int count;
	
	private long startTime;
	private long bytesWritten = 0;
	private long useTime = 0;
	private long numUses = 0;
	
	public MyChunkBuffer(final int x, final int z, final MyRegionFile file) {
		this.file = file;
		this.chunkX = x;
		this.chunkZ = z;

		this.buf = new byte[DEFAULT_BUFFER_SIZE];
		this.reset();
	}

	public void reset() {
		// Leave space for the header
		count = CHUNK_HEADER_SIZE;
		
		if(DO_TIMINGS)
			this.startTime = System.nanoTime();
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
        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * Writes the complete contents of this byte array output stream to
     * the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param      out   the output stream to which to write the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public byte toByteArray()[] {
        return Arrays.copyOf(buf, count);
    }

    public String toString() {
        return new String(buf, 0, count);
    }

    public String toString(String charsetName)
        throws UnsupportedEncodingException
    {
        return new String(buf, 0, count, charsetName);
    }

	public void close() throws IOException {

		// Fill out the header before
		// passing off.  It's BigEndian, and
		// includes the byte for compression
		// type - not sure why.
		final int len = count - CHUNK_HEADER_SIZE + 1;
		buf[0] = (byte) ((len >>> 24) & 0xFF);
		buf[1] = (byte) ((len >>> 16) & 0xFF);
		buf[2] = (byte) ((len >>> 8) & 0xFF);
		buf[3] = (byte) ((len >>> 0) & 0xFF);
		
		// Set our compression type - currently
		// Inflate/Deflate.
		buf[4] = 2; // COMPRESSION_FLATION

		file.write(chunkX, chunkZ, buf, count);
		file = null;
		freeChunkBuffers.add(this);
		
		if(DO_TIMINGS) {
			bytesWritten += count;
			useTime += System.nanoTime() - startTime;
			numUses++;
			final float msecs = (useTime / numUses / 1000F);
			final float avgBytes = (float)bytesWritten / numUses;
			final float rate = (avgBytes / msecs);
			System.out.println(String.format("MyChunkBuffer time %f, size %f (rate %f b/msec)", msecs, avgBytes, rate ));
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
