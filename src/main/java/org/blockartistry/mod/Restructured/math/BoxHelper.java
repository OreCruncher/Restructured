package org.blockartistry.mod.Restructured.math;

import java.util.ArrayList;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public final class BoxHelper {

	public static class RegionStats {

		public int area;
		public double mean;
		public double variance;

		@Override
		public String toString() {
			return String.format(
					"[area: %d; mean: %-1.2f; variance: %-1.2f]", area,
					mean, variance);
		}
	};

	public static RegionStats getRegionStats(World world,
			StructureBoundingBox worldBB, StructureBoundingBox structBB) {
		ArrayList<Integer> data = new ArrayList<Integer>();
		int avgGroundLevel = world.provider.getAverageGroundLevel() - 1;
		
		int total = 0;
		int count = 0;

		// Collect the data points
		for (int z = structBB.minZ; z <= structBB.maxZ; ++z)
			for (int x = structBB.minX; x <= structBB.maxX; ++x)
				if ((structBB == worldBB) || worldBB.isVecInside(x, structBB.minY, z)) {
					int val = Math.max(world.getTopSolidOrLiquidBlock(x, z), avgGroundLevel); 
					total += val;
					count++;
					data.add(val);
				}

		RegionStats result = new RegionStats();
		result.area = count;
		result.mean = (double) total / (double) count;

		double accum = 0;
		for (int i : data) {
			double t = ((double)i - result.mean);
			accum += (t * t);
		}

		result.variance = accum / (double) count;

		return result;
	}
}
