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

import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.assets.SchematicWeightItem;
import org.blockartistry.mod.Restructured.util.WeightTable;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.registry.GameRegistry;

public class SchematicWorldGenHandler implements IWorldGenerator {

	private static int CHUNK_SIZE = 16;
	private static SchematicProperties noSpawnSentinel = new SchematicProperties();
	private static int MINIMUM_SPAWN_DISTANCE = 8;
	private static int MINIMUM_VILLAGE_DISTANCE = 10 * CHUNK_SIZE;

	public SchematicWorldGenHandler() {
		GameRegistry.registerWorldGenerator(this, 200);
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

		// Don't want to spawn too close to the world spawn point
		int spawnChunkX = Math.abs(chunkX - (world.getSpawnPoint().posX / CHUNK_SIZE));
		int spawnChunkZ = Math.abs(chunkZ - (world.getSpawnPoint().posZ / CHUNK_SIZE));
		if (spawnChunkX < MINIMUM_SPAWN_DISTANCE
				|| spawnChunkZ < MINIMUM_SPAWN_DISTANCE)
			return;

		// Figure the x and z in the current chunk
		int x = (chunkX * CHUNK_SIZE) + random.nextInt(CHUNK_SIZE);
		int z = (chunkZ * CHUNK_SIZE) + random.nextInt(CHUNK_SIZE);

		// See if we are too close to a village
		if (world.villageCollectionObj != null && world.villageCollectionObj.findNearestVillage(x,
				world.provider.getAverageGroundLevel(), z, MINIMUM_VILLAGE_DISTANCE) != null)
			return;

		int dimension = world.provider.dimensionId;
		BiomeGenBase b = world.getBiomeGenForCoords(chunkX, chunkZ);

		// Find applicable structures for this attempt. If there aren't
		// any return.
		WeightTable<SchematicWeightItem> structs = Assets.getTableForWorldGen(
				dimension, b.biomeID);
		if (structs.size() == 0)
			return;
		
		// Only 1 in 100 chunks will have a chance.  Add a no
		// spawn sentinel at 99 times the total weight of the current
		// weight table.
		noSpawnSentinel.worldWeight = structs.getTotalWeight() * 99;
		structs.add(new SchematicWeightItem(noSpawnSentinel, false));

		// Assuming we get here are are going for it
		SchematicProperties props = structs.next().properties;
		if(props == noSpawnSentinel)
			return;
		
		// Get a random orientation and build the structure
		int direction = random.nextInt(4);
		SchematicWorldGenStructure structure = new SchematicWorldGenStructure(
				world, direction, x, z, props);
		structure.build();
	}
}
