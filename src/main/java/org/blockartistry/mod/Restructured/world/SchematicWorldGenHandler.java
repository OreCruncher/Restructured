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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.blockartistry.mod.Restructured.ModOptions;
import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.assets.SchematicWeightItem;
import org.blockartistry.mod.Restructured.util.ElementRule;
import org.blockartistry.mod.Restructured.util.WeightTable;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.village.Village;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SchematicWorldGenHandler implements IWorldGenerator {

	private static final ElementRule dimensionRule = ModOptions.getGlobalDimensionRule();

	private static final int ONE_IN_N_CHANCE = ModOptions.getGenerationChance();
	private static final int MINIMUM_SPAWN_DISTANCE = 4; // chunks
	private static final int MINIMUM_VILLAGE_DISTANCE_SQUARED = 64; // chunks
	private static final int MINIMUM_GEN_DISTANCE_SQUARED = 64; // chunks

	private static final Set<ChunkCoordIntPair> activeGeneration = new HashSet<ChunkCoordIntPair>();

	private static BlockPos getRandomStart(final Random rand, final int chunkX, final int chunkZ) {
		final int x = (chunkX << 4) + 3 + rand.nextInt(8);
		final int z = (chunkZ << 4) + 3 + rand.nextInt(8);
		return new BlockPos(x, 0, z);
	}

	private static long distanceSq(final ChunkCoordIntPair pt1, final ChunkCoordIntPair pt2) {
		final long dX = pt1.chunkXPos - pt2.chunkXPos;
		final long dZ = pt1.chunkZPos - pt2.chunkZPos;
		return dX * dX + dZ * dZ;
	}

	private static long distanceSq(final BlockPos pt1, final BlockPos pt2) {
		final long dX = (pt1.getX() - pt2.getX()) >> 4;
		final long dZ = (pt1.getZ() - pt2.getZ()) >> 4;
		return dX * dX + dZ * dZ;
	}

	private static boolean tooCloseToOtherGen(final ChunkCoordIntPair loc) {

		synchronized (activeGeneration) {
			if (activeGeneration.isEmpty())
				return false;

			for (final ChunkCoordIntPair a : activeGeneration)
				if (distanceSq(loc, a) <= MINIMUM_GEN_DISTANCE_SQUARED)
					return true;
		}

		return false;
	}

	public SchematicWorldGenHandler() {
		GameRegistry.registerWorldGenerator(this, 200);
	}

	private static boolean anyVillagesTooClose(final World world, final BlockPos loc) {

		// Sometimes it can be null during initial map start
		if (world.villageCollectionObj == null)
			return false;

		final List<Village> villageList = world.villageCollectionObj.getVillageList();

		for (final Village v : villageList)
			if (distanceSq(loc, v.getCenter()) < MINIMUM_VILLAGE_DISTANCE_SQUARED)
				return true;

		return false;
	}

	private static boolean tooCloseToSpawn(final World world, final BlockPos loc) {
		return distanceSq(loc, world.getSpawnPoint()) < MINIMUM_SPAWN_DISTANCE;
	}

	private static EnumFacing randomDirection(final Random random) {
		return EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
	}

	@Override
	public void generate(final Random random, final int chunkX, final int chunkZ, final World world,
			final IChunkProvider chunkGenerator, final IChunkProvider chunkProvider) {

		// Saftey check...
		if (world.isRemote || !dimensionRule.isOk(world.provider.getDimensionId()))
			return;

		// Only want to generate if world structures are enabled
		if (!world.getWorldInfo().isMapFeaturesEnabled())
			return;

		// Chance to even go forward with gen
		if (ONE_IN_N_CHANCE < 1 || random.nextInt(ONE_IN_N_CHANCE) > 0)
			return;

		// See if there is some other gen occuring close by. This
		// gen can be forcing other chunks to initialize thus triggering
		// generation attempts. We don't want to step on each other.
		final ChunkCoordIntPair currentGen = new ChunkCoordIntPair(chunkX, chunkZ);
		synchronized (activeGeneration) {
			if (tooCloseToOtherGen(currentGen) || !activeGeneration.add(currentGen))
				return;
		}

		try {
			// Figure the x and z in the target chunk
			final BlockPos start = getRandomStart(random, chunkX, chunkZ);

			// See if we are too close to a village or to world spawn
			if (anyVillagesTooClose(world, start) || tooCloseToSpawn(world, start))
				return;

			// Obtain parameters for matching possible schematics for the area
			final int dimension = world.provider.getDimensionId();
			final BiomeGenBase biome = BiomeHelper.chunkBiomeSurvey(world, chunkGenerator.provideChunk(chunkX, chunkZ),
					random);

			// Find applicable structures for this attempt. If there aren't
			// any return.
			final WeightTable<SchematicWeightItem> structs = Assets.getTableForWorldGen(dimension, biome);
			if (structs.size() == 0)
				return;

			// Pick a schematic and build it
			final SchematicProperties props = structs.next().properties;
			final EnumFacing orientation = randomDirection(random);
			final SchematicWorldGenStructure structure = new SchematicWorldGenStructure(world, biome, orientation,
					start.getX(), start.getZ(), props);
			structure.build();

		} finally {
			// Remove our bread crumb - we are done
			synchronized (activeGeneration) {
				activeGeneration.remove(currentGen);
			}
		}
	}
}
