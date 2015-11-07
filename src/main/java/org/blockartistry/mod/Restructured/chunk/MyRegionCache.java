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
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import org.blockartistry.mod.Restructured.chunk.MyRegionFile;

/**
 * Replaces Minecraft's RegionFileCache.  It improves on Minecraft's implementation
 * in the following ways:
 * 
 * + The cache is LRU based.  When the number of cached RegionFiles exceeds the
 * cache size the oldest access RegionFile is closed and evicted.  Minecraft's
 * implementation purges the entire list when the cache size exceeded which in
 * turn forces a reload of RegionFiles.
 * 
 * + Locking of the region cache is more fine grained which in turn will reduce
 * contention between multiple threads.
 *
 */
public class MyRegionCache {

	private static final RegionFileLRU regionsByFilename = new RegionFileLRU();

	public static MyRegionFile createOrLoadRegionFile(final File saveDir, final int blockX, final int blockZ) {

		final StringBuilder builder = new StringBuilder(64);

		final int X = blockX >> 5;
		final int Z = blockZ >> 5;
		
		final File file = new File(saveDir, "region");
		final File file1 = new File(file,
				builder.append("r.").append(X).append('.').append(Z).append(".mca").toString());
		
		final String key = file1.toString();

		MyRegionFile regionfile = null;
		synchronized (regionsByFilename) {
			regionfile = regionsByFilename.get(key);
			if (regionfile == null) {
				file.mkdirs();
				regionfile = new MyRegionFile(file1);
				regionsByFilename.put(key, regionfile);
			}
		}

		return regionfile;
	}

	public static void clearRegionFileReferences() {

		synchronized (regionsByFilename) {

			for (final MyRegionFile rf : regionsByFilename.values()) {
				try {
					rf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			regionsByFilename.clear();
		}
	}

	public static DataInputStream getChunkInputStream(File saveDir, int blockX, int blockZ) {
		final MyRegionFile regionfile = createOrLoadRegionFile(saveDir, blockX, blockZ);
		return regionfile.getChunkDataInputStream(blockX & 31, blockZ & 31);
	}

	public static DataOutputStream getChunkOutputStream(File saveDir, int blockX, int blockZ) {
		final MyRegionFile regionfile = createOrLoadRegionFile(saveDir, blockX, blockZ);
		return regionfile.getChunkDataOutputStream(blockX & 31, blockZ & 31);
	}
}
