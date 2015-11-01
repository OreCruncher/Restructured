/*
 * This file is part of Restructured, licensed under the MIT License (MIT).
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import org.blockartistry.mod.Restructured.ModLog;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;

public final class MyRegionFileCache {

	private static final float HASHTABLE_LOAD_FACTOR = 0.75f;
	private static final int CACHE_SIZE = 256; // Vanilla is 256 - 1
	private static Map<File, RegionFile> regionFileCache = null;

	public static void initialize() {

		try {

			// Get the map of regions from Minecraft
			final Field cache = ReflectionHelper.findField(RegionFileCache.class, "regionsByFilename", "field_76553_a");

			// remove final modifier from field
			final Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(cache, cache.getModifiers() & ~Modifier.FINAL);

			// Create our replacement LinkedHashMap
			final int hashTableCapacity = (int) Math.ceil(CACHE_SIZE / HASHTABLE_LOAD_FACTOR) + 1;
			regionFileCache = new LinkedHashMap<File, RegionFile>(hashTableCapacity, HASHTABLE_LOAD_FACTOR, true) {
				// (an anonymous inner class)
				private static final long serialVersionUID = 1;

 				@Override
				protected boolean removeEldestEntry(final Map.Entry<File, RegionFile> eldest) {

					// If we have space in the cache, keep it. Note that we
					// cannot invoke size() on the LinkedHashMap because we
					// override it to return 0 to fool Minecraft.
					if (entrySet().size() <= CACHE_SIZE)
						return false;

					// Make sure the file is closed
					try {
						eldest.getValue().close();
					} catch (IOException e) {
						ModLog.warn("Unable to close region file '%s'", eldest.getKey().toString());
						e.printStackTrace();
					}

					// Remove it
					return true;
				}

				// Dangerous override. This is to fool the RegionFileCache into
				// thinking it has space in the cache and avoid purging. Only
				// reason it's here is because of looking into the code and
				// understanding what this method is being used for.
				@Override
				public int size() {
					return 0;
				}
			};

			// Replace Minecraft's HashMap with our LinkedHashMap implementation
			cache.set(null, regionFileCache);

		} catch (final Throwable t) {
			ModLog.error("Unable to hook region file cache", t);
		}
	}
}
