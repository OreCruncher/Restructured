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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Replacement for the standard DataInputStream that is returned
 * from RegionFile.  The goal is to cache ChunkInputStream objects
 * for reuse, but currently AnvilChunkLoader does not issue a
 * close() on the stream once it is done reading.  It needs to be
 * fixed to issue that close().
 */
public class ChunkInputStream extends DataInputStream {

	private final static ConcurrentLinkedQueue<ChunkInputStream> freeInputStreams = new ConcurrentLinkedQueue<ChunkInputStream>();

	public static ChunkInputStream getStream(final byte[] bits, final int dataLength) {
		ChunkInputStream buffer = freeInputStreams.poll();
		if(buffer == null)
			buffer = new ChunkInputStream();
		
		return buffer.reset(bits, dataLength);
	}
	
	private final static int CHUNK_HEADER_SIZE = 5;
	
	private Inflater inflater;
	private ByteArrayInputStream input;
	private InflaterInputStream inflaterStream;

	public ChunkInputStream() {
		super(null);
		
		this.inflater = new Inflater();
	}

	protected ChunkInputStream reset(final byte[] buffer, final int dataLength) {
		this.input = new ByteArrayInputStream(buffer, CHUNK_HEADER_SIZE, dataLength);
		this.inflater.reset();
		this.inflaterStream = new InflaterInputStream(this.input, inflater);
		this.in = this.inflaterStream;
		return this;
	}
	
	@Override
	public void close() throws IOException {
		// Close out the underlying stream and queue
		// for reuse.
		this.in.close();
		freeInputStreams.add(this);
	}
}
