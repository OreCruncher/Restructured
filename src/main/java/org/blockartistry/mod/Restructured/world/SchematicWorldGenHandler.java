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

import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.assets.SchematicWeightItem;
import org.blockartistry.mod.Restructured.util.WeightTable;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.registry.GameRegistry;

public class SchematicWorldGenHandler implements IWorldGenerator {

	private static final int CHUNK_SIZE = 16;
	private static final SchematicProperties NOSPAWN_SENTINEL = new SchematicProperties();
	private static final int MINIMUM_SPAWN_DISTANCE = 4; // chunks
	private static final int MINIMUM_VILLAGE_DISTANCE_SQUARED = 8 * 8
			* CHUNK_SIZE * CHUNK_SIZE; // blocks
	private static final int MINIMUM_GEN_DISTANCE_SQUARED = 16; // chunks

	private static Set<ChunkCoordinates> activeGeneration = new HashSet<ChunkCoordinates>();

	private static ChunkCoordinates getRandomStart(Random rand, int chunkX,
			int chunkZ) {
		int x = (chunkX << 4) + 3 + rand.nextInt(8);
		int z = (chunkZ << 4) + 3 + rand.nextInt(8);
		return new ChunkCoordinates(x, 0, z);
	}

	private static boolean tooCloseToOtherGen(ChunkCoordinates loc) {
		if (activeGeneration.isEmpty())
			return false;

		for (ChunkCoordinates a : activeGeneration) {
			int distance = (int) loc.getDistanceSquaredToChunkCoordinates(a);
			if (distance <= MINIMUM_GEN_DISTANCE_SQUARED)
				return true;
		}

		return false;
	}

	public SchematicWorldGenHandler() {
		GameRegistry.registerWorldGenerator(this, 200);
	}

	private static boolean anyVillagesTooClose(World world, ChunkCoordinates loc) {

		// Sometimes it can be null during initial map start
		if (world.villageCollectionObj == null)
			return false;

		List<?> villageList = world.villageCollectionObj.getVillageList();

		for (Object o : villageList) {
			ChunkCoordinates coords = ((Village) o).getCenter();
			coords.posY = loc.posY;
			int distance = (int) coords
					.getDistanceSquaredToChunkCoordinates(loc);
			if (distance < MINIMUM_VILLAGE_DISTANCE_SQUARED)
				return true;
		}

		return false;
	}

	private static boolean tooCloseToSpawn(World world, ChunkCoordinates loc) {
		ChunkCoordinates coords = world.getSpawnPoint();
		coords.posY = loc.posY;
		return (int) coords.getDistanceSquaredToChunkCoordinates(loc) < MINIMUM_SPAWN_DISTANCE;
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {

		// Not sure this can happen, but...
		if (world.isRemote)
			return;

		// Only want to generate if world structures are enabled
		if (!world.getWorldInfo().isMapFeaturesEnabled())
			return;

		// See if there is some other gen occuring close by. This
		// gen can be forcing other chunks to initialize thus triggering
		// generation attempts. We don't want to step on each other.
		ChunkCoordinates currentGen = new ChunkCoordinates(chunkX, 0, chunkZ);
		if (tooCloseToOtherGen(currentGen))
			return;

		// Leave our bread crumb
		activeGeneration.add(currentGen);

		try {
			// set our random
			random = world.setRandomSeed(chunkX, chunkZ, 0xdeadbeef);

			// Figure the x and z in the current chunk
			ChunkCoordinates start = getRandomStart(random, chunkX, chunkZ);

			// See if we are too close to a village or to world spawn
			if (anyVillagesTooClose(world, start)
					|| tooCloseToSpawn(world, start))
				return;

			int dimension = world.provider.dimensionId;
			BiomeGenBase biome = BiomeHelper.chunkBiomeSurvey(world,
					chunkGenerator.provideChunk(chunkX, chunkZ));

			// Find applicable structures for this attempt. If there aren't
			// any return.
			WeightTable<SchematicWeightItem> structs = Assets
					.getTableForWorldGen(dimension, biome);
			if (structs.size() == 0)
				return;

			// Only 1 in 100 chunks will have a chance. Add a no
			// spawn sentinel at 99 times the total weight of the current
			// weight table.
			NOSPAWN_SENTINEL.worldWeight = structs.getTotalWeight() * 99;
			structs.add(new SchematicWeightItem(NOSPAWN_SENTINEL, false));

			// Assuming we get here are are going for it
			SchematicProperties props = structs.next().properties;
			if (props == NOSPAWN_SENTINEL)
				return;

			// Get a random orientation and build the structure
			int direction = random.nextInt(4);

			SchematicWorldGenStructure structure = new SchematicWorldGenStructure(
					world, biome, direction, start.posX, start.posZ, props);
			structure.build();

		} finally {
			// Remove our bread crumb - we are done
			activeGeneration.remove(currentGen);
		}
	}
}
