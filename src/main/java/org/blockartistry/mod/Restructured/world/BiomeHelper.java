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

package org.blockartistry.mod.Restructured.world;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public final class BiomeHelper {

	// Horseshoes and hand grenades...
	private final static int SAMPLES = 32;

	private BiomeHelper() {
	}

	/**
	 * Analyze the chunk to estimate the predominant biome that is present. The
	 * predominant biome will be used to filter the weight list and make further
	 * decisions.
	 * 
	 * Check ChunkProviderGenerate for some detail as to the chunk biome array.
	 * 
	 * @param world
	 *            Current world
	 * @param chunk
	 *            The chunk to analyze
	 * @return Estimated predominant biome in the indicated chunk
	 */
	public static BiomeGenBase chunkBiomeSurvey(final World world, final Chunk chunk, final Random rand) {

		final byte[] biomes = chunk.getBiomeArray();
		final int[] counts = new int[biomes.length];

		int highIndex = BiomeGenBase.plains.biomeID;
		int highCount = -1;

		for (int i = 0; i < SAMPLES; i++) {
			final int id = biomes[rand.nextInt(biomes.length)] & 255;

			// -1 in byte terms
			if (id == 255)
				continue;

			if (++counts[id] > highCount) {
				highIndex = id;
				highCount = counts[id];
			}
		}
		return BiomeGenBase.getBiome(highIndex);
	}
}
