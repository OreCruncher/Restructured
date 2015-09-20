package org.blockartistry.mod.Restructured.world.village.themes;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

public class ForestVillageTheme extends VillageTheme {

	public static void initialize() {
		
		final HashMap<Block, Integer> mappings = new HashMap<Block, Integer>();

		// Replace with oak
		mappings.put(Blocks.log, encode(Blocks.log, 0));
		mappings.put(Blocks.log2, encode(Blocks.log, 0));
		mappings.put(Blocks.planks, encode(Blocks.planks, 0));
		mappings.put(Blocks.spruce_stairs, encode(Blocks.oak_stairs, -1));
		mappings.put(Blocks.birch_stairs, encode(Blocks.oak_stairs, -1));
		mappings.put(Blocks.dark_oak_stairs, encode(Blocks.oak_stairs, -1));
		mappings.put(Blocks.jungle_stairs, encode(Blocks.oak_stairs, -1));
		mappings.put(Blocks.acacia_stairs, encode(Blocks.oak_stairs, -1));
		mappings.put(Blocks.wooden_slab, encode(Blocks.wooden_slab, 0));
		mappings.put(Blocks.double_wooden_slab, encode(Blocks.double_wooden_slab, 0));
		
		register(BiomeGenBase.forest, new ForestVillageTheme(BiomeGenBase.forest, mappings));
		register(BiomeGenBase.forestHills, new ForestVillageTheme(BiomeGenBase.forestHills, mappings));
	}
	
	protected ForestVillageTheme(BiomeGenBase biome, Map<Block, Integer> mapping) {
		super(biome, mapping);
	}
}
