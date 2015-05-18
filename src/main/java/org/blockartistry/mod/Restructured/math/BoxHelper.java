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

package org.blockartistry.mod.Restructured.math;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public final class BoxHelper {

	public static class RegionStats {

		public int area;
		public int mean;
		public int variance;

		@Override
		public String toString() {
			return String.format(
					"[area: %d; mean: %d; variance: %d]", area,
					mean, variance);
		}
	};

	// Note that this routine can trigger chunk loading and generation
	// as it traverses a region.
	public static RegionStats getRegionStatsWithVariance(final World world,
			final StructureBoundingBox worldBB) {

		final int avgGroundLevel = world.provider.getAverageGroundLevel() - 1;
		final RegionStats result = new RegionStats();
		result.area = worldBB.getXSize() * worldBB.getZSize();
		
		final int[] data = new int[result.area];
		int total = 0;
		int index = 0;

		for (int z = worldBB.minZ; z <= worldBB.maxZ; ++z)
			for (int x = worldBB.minX; x <= worldBB.maxX; ++x) {
				int val = Math.max(world.getTopSolidOrLiquidBlock(x, z), avgGroundLevel); 
				total += val;
				data[index++] = val;
			}

		result.mean = Math.round((float) total / (float) index);

		int accum = 0;
		for (int i = 0; i < index; i++) {
			int t = data[i] - result.mean;
			accum += (t * t);
		}

		result.variance = Math.round((float)accum / (float) index);

		return result;
	}

	public static int getRegionAverageGroundLevel(final World world,
			final StructureBoundingBox worldBB) {

		final int avgGroundLevel = world.provider.getAverageGroundLevel() - 1;
		final int area = worldBB.getXSize() * worldBB.getZSize();
		
		int total = 0;

		for (int z = worldBB.minZ; z <= worldBB.maxZ; ++z)
			for (int x = worldBB.minX; x <= worldBB.maxX; ++x) {
				total += Math.max(world.getTopSolidOrLiquidBlock(x, z), avgGroundLevel);
			}

		return Math.round(total / area);
	}
}
