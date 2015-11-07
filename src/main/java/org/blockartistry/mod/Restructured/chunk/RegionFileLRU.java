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
import java.util.LinkedHashMap;
import java.util.Map;

import org.blockartistry.mod.Restructured.ModLog;

/**
 * Implements a cache of RegionFiles that evicts oldest RegionFile from the
 * cache when it's size is exceeded.
 */
@SuppressWarnings("serial")
public class RegionFileLRU extends LinkedHashMap<String, MyRegionFile> {
	
	private static final float HASHTABLE_LOAD_FACTOR = 0.75f;
	private static final int CACHE_SIZE = 256; // Vanilla is 256 - 1
	private static final int HASHTABLE_CAPACITY = (int) Math.ceil(CACHE_SIZE / HASHTABLE_LOAD_FACTOR) + 1;

	public RegionFileLRU() {
		super(HASHTABLE_CAPACITY, HASHTABLE_LOAD_FACTOR, true);
	}

	@Override
	protected boolean removeEldestEntry(final Map.Entry<String, MyRegionFile> eldest) {

		final MyRegionFile rf = eldest.getValue();
		
		// If we have space in the cache and the file is not
		// idle, keep.
		if (size() <= CACHE_SIZE && !rf.isIdle())
			return false;

		// Make sure the file is closed
		try {
			rf.close();
		} catch (IOException e) {
			ModLog.warn("Unable to close region file '%s'", eldest.getKey().toString());
			e.printStackTrace();
		}

		// Remove it
		return true;
	}
}
