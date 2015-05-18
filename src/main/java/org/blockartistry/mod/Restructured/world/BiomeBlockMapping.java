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

import java.util.HashMap;

import org.blockartistry.mod.Restructured.util.SelectedBlock;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;

public final class BiomeBlockMapping {

	private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
	private static final int META_MASK = 0xFF;
	private static final int BLOCK_SHIFT = 8;

	private static int encode(Block block, int meta) {
		return BLOCK_REGISTRY.getId(block) << BLOCK_SHIFT | (meta & META_MASK);
	}
	
	private static Block decodeBlock(int code) {
		return BLOCK_REGISTRY.getObjectById(code >> BLOCK_SHIFT);
	}
	
	private static int decodeMeta(int code) {
		return code & META_MASK;
	}
	
	protected static final HashMap<BiomeGenBase, HashMap<Block, Integer>> replacements = new HashMap<BiomeGenBase, HashMap<Block, Integer>>();
	
	static {
		
		HashMap<Block, Integer> temp = new HashMap<Block, Integer>();
		
		temp.put(Blocks.log, encode(Blocks.sandstone, 0));
		temp.put(Blocks.log2, encode(Blocks.sandstone, 0));
		temp.put(Blocks.cobblestone, encode(Blocks.sandstone, 0));
		temp.put(Blocks.planks, encode(Blocks.sandstone, 2));
		temp.put(Blocks.oak_stairs, encode(Blocks.sandstone_stairs, -1));
		temp.put(Blocks.stone_stairs, encode(Blocks.sandstone_stairs, -1));
		temp.put(Blocks.spruce_stairs, encode(Blocks.sandstone_stairs, -1));
		temp.put(Blocks.birch_stairs, encode(Blocks.sandstone_stairs, -1));
		temp.put(Blocks.dark_oak_stairs, encode(Blocks.sandstone_stairs, -1));
		temp.put(Blocks.gravel, encode(Blocks.sandstone, -1));
		temp.put(Blocks.wooden_slab, encode(Blocks.stone_slab, 1));
		temp.put(Blocks.stone_slab, encode(Blocks.stone_slab, 1));
		temp.put(Blocks.double_wooden_slab, encode(Blocks.sandstone, 0));
		temp.put(Blocks.dirt, encode(Blocks.sand, 0));
		temp.put(Blocks.grass, encode(Blocks.sand, 0));
		temp.put(Blocks.red_flower, encode(Blocks.deadbush, 0));
		temp.put(Blocks.yellow_flower, encode(Blocks.deadbush, 0));
		temp.put(Blocks.red_mushroom, encode(Blocks.air, 0));
		temp.put(Blocks.brown_mushroom, encode(Blocks.air, 0));
		temp.put(Blocks.double_plant, encode(Blocks.air, 0));
		temp.put(Blocks.tallgrass, encode(Blocks.air, 0));
		replacements.put(BiomeGenBase.desert, temp);
		replacements.put(BiomeGenBase.desertHills, temp);

		temp = new HashMap<Block, Integer>();
		temp.put(Blocks.log, encode(Blocks.snow, 0));
		temp.put(Blocks.log2, encode(Blocks.snow, 0));
		temp.put(Blocks.cobblestone, encode(Blocks.packed_ice, 0));
		temp.put(Blocks.planks, encode(Blocks.snow, 0));
		temp.put(Blocks.gravel, encode(Blocks.packed_ice, 0));
		temp.put(Blocks.dirt, encode(Blocks.snow, 0));
		temp.put(Blocks.grass, encode(Blocks.snow, 0));
		temp.put(Blocks.red_flower, encode(Blocks.air, 0));
		temp.put(Blocks.yellow_flower, encode(Blocks.air, 0));
		temp.put(Blocks.red_mushroom, encode(Blocks.air, 0));
		temp.put(Blocks.brown_mushroom, encode(Blocks.air, 0));
		temp.put(Blocks.double_plant, encode(Blocks.air, 0));
		temp.put(Blocks.tallgrass, encode(Blocks.air, 0));
		replacements.put(BiomeGenBase.iceMountains, temp);
		replacements.put(BiomeGenBase.icePlains, temp);
	}

	protected static SelectedBlock scrubEggs(Block block, int meta) {
		// If it is a monster egg scrub it
		if (block == Blocks.monster_egg)
			switch (meta) {
			case 0:
				block = Blocks.stone;
				meta = 0;
				break;
			case 1:
				block = Blocks.cobblestone;
				meta = 0;
				break;
			case 2:
				block = Blocks.stonebrick;
				meta = 0;
				break;
			case 3:
				block = Blocks.stonebrick;
				meta = 1;
				break;
			case 4:
				block = Blocks.stonebrick;
				meta = 2;
			case 5:
				block = Blocks.stonebrick;
				meta = 3;
				break;
			default:
				;
			}
		return new SelectedBlock(block, meta);
	}
	
	protected static Block _findReplacementBlock(BiomeGenBase biome, Block block, int meta) {
		// Biome hooks always get first crack
		BiomeEvent.GetVillageBlockID event = new BiomeEvent.GetVillageBlockID(
				biome, block, meta);
		MinecraftForge.TERRAIN_GEN_BUS.post(event);
		if (event.getResult() == Result.DENY)
			return event.replacement;

		Block replace = null;
		HashMap<Block, Integer> map = replacements.get(biome);
		if(map != null) {
			Integer code = map.get(block);
			if(code != null)
				replace = decodeBlock(code);
		}
		
		return (replace == null) ? block : replace;
	}
	
	public static int _findReplacementMeta(BiomeGenBase biome, Block block, int meta) {
		
		// Biome hooks always get first crack
		BiomeEvent.GetVillageBlockMeta event = new BiomeEvent.GetVillageBlockMeta(
				biome, block, meta);
		MinecraftForge.TERRAIN_GEN_BUS.post(event);
		if (event.getResult() == Result.DENY)
			return event.replacement;

		HashMap<Block, Integer> map = replacements.get(biome);
		int replace = META_MASK;
		if(map != null) {
			Integer code = map.get(block);
			if(code != null)
				replace = decodeMeta(code);
		}

		if(replace != META_MASK) {
			// Preserve slab orientation
			if(block == Blocks.wooden_slab || block == Blocks.stone_slab)
				meta = replace | (meta & 8);
			else
				meta = replace;
		}
		
		return meta; 
	}

	public static SelectedBlock findReplacement(BiomeGenBase biome, Block block, int meta) {
		SelectedBlock b = scrubEggs(block, meta);
		Block bx = _findReplacementBlock(biome, b.getBlock(), b.getMeta());
		int mx = _findReplacementMeta(biome, b.getBlock(), b.getMeta());
		return new SelectedBlock(bx, mx);
	}
	
	public static Block findReplacementBlock(BiomeGenBase biome, Block block, int meta) {
		SelectedBlock b = scrubEggs(block, meta);
		return _findReplacementBlock(biome, b.getBlock(), b.getMeta());
	}
	
	public static int findReplacementMeta(BiomeGenBase biome, Block block, int meta) {
		SelectedBlock b = scrubEggs(block, meta);
		return _findReplacementMeta(biome, b.getBlock(), b.getMeta());
	}
}
