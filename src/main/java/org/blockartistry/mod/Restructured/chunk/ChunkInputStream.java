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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Replacement for the standard DataInputStream that is returned from
 * RegionFile.
 */
public class ChunkInputStream extends DataInputStream {

	// Size limit of the buffer that is kept around. Defaults
	// to 8 sectors, and may need to be tuned based on modpack
	// behaviors.
	private static final int INPUT_BUFFER_SIZE_LIMIT = 4096 * 8;

	private static final int CHUNK_HEADER_SIZE = 5;

	private static final AtomicInteger streamNumber = new AtomicInteger();

	private final static ConcurrentLinkedQueue<ChunkInputStream> freeInputStreams = new ConcurrentLinkedQueue<ChunkInputStream>();

	public static ChunkInputStream getStream() {
		final ChunkInputStream buffer = freeInputStreams.poll();
		return buffer != null ? buffer : new ChunkInputStream();
	}

	public static void returnStream(final ChunkInputStream stream) {
		if (stream != null)
			freeInputStreams.add(stream);
	}

	@SuppressWarnings("unused")
	private int myID = streamNumber.incrementAndGet();

	private byte[] inputBuffer;
	private Inflater inflater;
	private ByteArrayInputStreamNonAsync input;
	private InflaterInputStream inflaterStream;

	public ChunkInputStream() {
		super(null);

		inputBuffer = new byte[INPUT_BUFFER_SIZE_LIMIT];
		input = new ByteArrayInputStreamNonAsync();
		inflater = new Inflater();
	}

	public ChunkInputStream bake() {
		if (inputBuffer == null)
			inputBuffer = new byte[INPUT_BUFFER_SIZE_LIMIT];

		input.attach(inputBuffer, CHUNK_HEADER_SIZE, inputBuffer.length);
		inflater.reset();
		inflaterStream = new InflaterInputStream(input, inflater);
		in = inflaterStream;
		return this;
	}

	public byte[] getBuffer() {
		return inputBuffer;
	}

	/**
	 * Get's the buffer associated with the stream and ensures it is of
	 * an appropriate size.  The length of the buffer returned can be
	 * greater than requested.
	 * 
	 * @param desiredSize
	 * @return
	 */
	public byte[] getBuffer(final int desiredSize) {
		if (inputBuffer == null || desiredSize > inputBuffer.length)
			inputBuffer = new byte[Math.max(desiredSize, INPUT_BUFFER_SIZE_LIMIT)];

		return inputBuffer;
	}

	/**
	 * Attaches the buffer to this stream. The stream takes ownership.
	 * 
	 * @param buffer
	 * @return
	 */
	public byte[] setBuffer(final byte[] buffer) {
		inputBuffer = buffer;
		return buffer;
	}

	/**
	 * Resizes the buffer if necessary to stay within limits.
	 */
	public void trimBuffer() {
		if (inputBuffer != null && inputBuffer.length > INPUT_BUFFER_SIZE_LIMIT)
			inputBuffer = null;
	}

	@Override
	public void close() throws IOException {
		// Close out the underlying stream and queue
		// for reuse.
		if (in != null) {
			in.close();
			in = null;
			inflaterStream = null;
		}

		// Get the buffer back to normal if needed. Don't
		// want to keep very large buffers around if it
		// isn't needed.
		trimBuffer();

		// To the free list!
		freeInputStreams.add(this);
	}
}
