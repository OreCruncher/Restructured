package org.blockartistry.mod.Restructured.world;

import java.util.List;
import java.util.Random;

import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.assets.SchematicWeightItem;
import org.blockartistry.mod.Restructured.util.WeightTable;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

public class MapGenSchematicStructure extends MapGenStructure {

	private static final int CHUNK_SIZE = 16;
	private static final int CHUNK_HALFAREA = (CHUNK_SIZE * CHUNK_SIZE) / 2;
	private static final SchematicProperties NOSPAWN_SENTINEL = new SchematicProperties();
	private static final int MINIMUM_SPAWN_DISTANCE = 4; // chunks
	private static final int MINIMUM_VILLAGE_DISTANCE_SQUARED = 8 * 8 * CHUNK_SIZE * CHUNK_SIZE; // blocks

	private static ChunkCoordinates getRandomStart(Random rand, int chunkX, int chunkZ) {
		int x = (chunkX * CHUNK_SIZE) + 3 + rand.nextInt(8);
		int z = (chunkZ * CHUNK_SIZE) + 3 + rand.nextInt(8);
		return new ChunkCoordinates(x, 0, z);
	}
	
	private static boolean anyVillagesTooClose(World world, ChunkCoordinates loc) {
		
		// Sometimes it can be null during initial map start
		if(world.villageCollectionObj == null)
			return false;
		
		List<?> villageList = world.villageCollectionObj.getVillageList();
		
		for(Object o: villageList) {
			ChunkCoordinates coords = ((Village)o).getCenter();
			coords.posY = loc.posY;
			int distance = (int)coords.getDistanceSquaredToChunkCoordinates(loc);
			if(distance < MINIMUM_VILLAGE_DISTANCE_SQUARED)
				return true;
		}
		
		return false;
	}
	
	private static boolean tooCloseToSpawn(World world, ChunkCoordinates loc) {
		ChunkCoordinates coords = world.getSpawnPoint();
		coords.posY = loc.posY;
		return (int)coords.getDistanceSquaredToChunkCoordinates(loc) < MINIMUM_SPAWN_DISTANCE;
	}
	
	protected SchematicProperties toGen = null;
	
	/**
	 * Analyze the chunk to determine the predominant biome that is present. The
	 * predominant biome will be used to filter the weight list and make further
	 * decisions.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @return Predominant biome in the indicated chunk
	 */
	protected BiomeGenBase chunkBiomeSurvey(World world, int chunkX, int chunkZ) {

		final int xStart = chunkX * CHUNK_SIZE;
		final int zStart = chunkZ * CHUNK_SIZE;
		final int[] counts = new int[BiomeGenBase.getBiomeGenArray().length];

		int highIndex = -1;
		int highCount = -1;

		for (int zIdx = 0; zIdx < CHUNK_SIZE; zIdx++) {
			int z = zStart + zIdx;
			for (int xIdx = 0; xIdx < CHUNK_SIZE; xIdx++) {
				int x = xStart + xIdx;
				BiomeGenBase b = world.getBiomeGenForCoords(x, z);
				if (b != null) {
					// Keep track of the high count
					if (++counts[b.biomeID] > highCount) {
						highIndex = b.biomeID;
						highCount = counts[highIndex];

						// If the count is more than half the area
						// leave - its going to be the winner.
						if (highCount >= CHUNK_HALFAREA)
							return BiomeGenBase.getBiome(highIndex);
					}
				}
			}
		}

		return BiomeGenBase.getBiome(highIndex);
	}

	
	@Override
	public String func_143025_a() {
		return "SchematicStructure";
	}

	@Override
	protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
		
		ChunkCoordinates currentGen = new ChunkCoordinates(chunkX, 0, chunkZ);
		
		Random random = this.worldObj.setRandomSeed(chunkX, chunkZ, 0xdeadbeef);

		// Figure the x and z in the current chunk
		ChunkCoordinates start = getRandomStart(random, chunkX, chunkZ);

		// See if we are too close to a village or to world spawn
		if (anyVillagesTooClose(this.worldObj, start) || tooCloseToSpawn(this.worldObj, start))
			return false;

		int dimension = this.worldObj.provider.dimensionId;
		BiomeGenBase biome = chunkBiomeSurvey(this.worldObj, chunkX, chunkZ);

		// Find applicable structures for this attempt. If there aren't
		// any return.
		WeightTable<SchematicWeightItem> structs = Assets
				.getTableForWorldGen(dimension, biome);
		if (structs.size() == 0)
			return false;

		// Only 1 in 100 chunks will have a chance. Add a no
		// spawn sentinel at 99 times the total weight of the current
		// weight table.
		NOSPAWN_SENTINEL.worldWeight = structs.getTotalWeight() * 99;
		structs.add(new SchematicWeightItem(NOSPAWN_SENTINEL, false));

		// Assuming we get here are are going for it
		this.toGen = structs.next().properties;
		if (this.toGen == NOSPAWN_SENTINEL)
			return false;

		// Get a random orientation and build the structure
		int direction = random.nextInt(4);
		/*
		SchematicWorldGenStructure structure = new SchematicWorldGenStructure(
				this.worldObj, biome, direction, start.posX, start.posZ, props);
		structure.build();
		*/
		return true;
	}

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenSchematicStructure.Start(this.worldObj, this.rand, chunkX, chunkZ);
    }

    public static class Start extends StructureStart
    {
        public Start() {}

        public Start(World world, Random random, int chunkX, int chunkZ)
        {
            super(chunkX, chunkZ);
            BiomeGenBase biomegenbase = world.getBiomeGenForCoords(chunkX * 16 + 8, chunkZ * 16 + 8);

            if (biomegenbase != BiomeGenBase.jungle && biomegenbase != BiomeGenBase.jungleHills)
            {
                if (biomegenbase == BiomeGenBase.swampland)
                {
                    ComponentScatteredFeaturePieces.SwampHut swamphut = new ComponentScatteredFeaturePieces.SwampHut(random, chunkX * 16, chunkZ * 16);
                    this.components.add(swamphut);
                }
                else
                {
                    ComponentScatteredFeaturePieces.DesertPyramid desertpyramid = new ComponentScatteredFeaturePieces.DesertPyramid(random, chunkX * 16, chunkZ * 16);
                    this.components.add(desertpyramid);
                }
            }
            else
            {
                ComponentScatteredFeaturePieces.JunglePyramid junglepyramid = new ComponentScatteredFeaturePieces.JunglePyramid(random, chunkX * 16, chunkZ * 16);
                this.components.add(junglepyramid);
            }

            this.updateBoundingBox();
        }
    }
}
