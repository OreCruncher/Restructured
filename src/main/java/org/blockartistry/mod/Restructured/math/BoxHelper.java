package org.blockartistry.mod.Restructured.math;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public final class BoxHelper {

	public static class RegionStats {

		public int area;
		public int mean;
		public int variance;
		public int deviation;

		@Override
		public String toString() {
			return String.format(
					"[area: %d; mean: %d; variance: %d; deviation: %d]", area,
					mean, variance, deviation);
		}
	};

	public static RegionStats getRegionStats(World world,
			StructureBoundingBox worldBB, StructureBoundingBox structBB) {
		ArrayList<Integer> data = new ArrayList<Integer>();
		int avgGroundLevel = world.provider.getAverageGroundLevel();

		// Collect the data points
		for (int z = structBB.minZ; z <= structBB.maxZ; ++z)
			for (int x = structBB.minX; x <= structBB.maxX; ++x)
				if (worldBB.isVecInside(x, structBB.minY, z))
					data.add(Math.max(world.getTopSolidOrLiquidBlock(x, z),
							avgGroundLevel));

		RegionStats result = new RegionStats();
		result.area = data.size();
		doMean(data, result);
		doVariance(data, result);
		doDeviation(data, result);

		return result;
	}

	static void doMean(List<Integer> list, RegionStats stats) {
		stats.mean = sum(list) / list.size();
	}

	static int sum(List<Integer> list) {
		int accum = 0;
		for (int i : list)
			accum += i;
		return accum;
	}

	static void doVariance(List<Integer> list, RegionStats stats) {
		double accum = 0;

		for (int i : list) {
			accum += Math.pow(i - stats.mean, 2);
		}

		stats.variance = (int) (accum / list.size());
	}

	static void doDeviation(List<Integer> list, RegionStats stats) {
		stats.deviation = (int) Math.sqrt(stats.variance);
	}
}
