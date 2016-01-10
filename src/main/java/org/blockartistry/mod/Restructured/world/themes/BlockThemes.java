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

package org.blockartistry.mod.Restructured.world.themes;

import java.util.IdentityHashMap;
import java.util.Map;

import org.blockartistry.mod.Restructured.util.BlockHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenMutated;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class BlockThemes {

	private static final Map<BiomeGenBase, Map<Block, ThemeBlock>> themes = new IdentityHashMap<BiomeGenBase, Map<Block, ThemeBlock>>();
	private static final int KEEP_META = -1;
	private static final ThemeBlock AIR = new ThemeBlock(Blocks.air);

	private static final IBlockState[] monsterBlockMap = new IBlockState[] { Blocks.stone.getStateFromMeta(0),
			Blocks.cobblestone.getStateFromMeta(0), Blocks.stonebrick.getStateFromMeta(0),
			Blocks.stonebrick.getStateFromMeta(1), Blocks.stonebrick.getStateFromMeta(2),
			Blocks.stonebrick.getStateFromMeta(3) };

	private static void register(final BiomeGenBase biome, final Map<Block, ThemeBlock> theme) {
		// Register biome
		themes.put(biome, theme);

		// Check for mutation
		final BiomeGenBase mutation = BiomeGenBase.getBiome(biome.biomeID + 128);
		if (mutation instanceof BiomeGenMutated)
			themes.put(mutation, theme);
	}

	/**
	 * Provides an alternative block if the input block is a monster egg.
	 */
	public static IBlockState scrubEggs(final IBlockState state) {
		if (!BlockHelper.isMonsterEgg(state))
			return state;
		final int idx = state.getBlock().getMetaFromState(state);
		if (idx >= monsterBlockMap.length)
			return state;
		return monsterBlockMap[idx];
	}

	/**
	 * Provides an alternative block if the input block is a fire source (can
	 * cause fire spread).
	 */
	public static IBlockState scrubFireSource(final IBlockState state) {
		if (BlockHelper.isFireSource(state.getBlock()))
			return Blocks.air.getDefaultState();
		return state;
	}

	/**
	 * Invokes the Forge events to figure out any block replacements due to
	 * themes.
	 */
	public static IBlockState findReplacement(final BiomeGenBase biome, final IBlockState state) {
		// Ask subscribers if they want to replace
		final BiomeEvent.GetVillageBlockID event1 = new BiomeEvent.GetVillageBlockID(biome, state);
		MinecraftForge.TERRAIN_GEN_BUS.post(event1);
		if (event1.getResult() == Result.DENY)
			return event1.replacement;
		return state;
	}

	public static void initialize() {

		// Beaches
		Map<Block, ThemeBlock> mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.dirt, new ThemeBlock(Blocks.sand, 0));
		mappings.put(Blocks.grass, new ThemeBlock(Blocks.sand, 0));
		mappings.put(Blocks.red_flower, new ThemeBlock(Blocks.deadbush, 0));
		mappings.put(Blocks.yellow_flower, new ThemeBlock(Blocks.deadbush, 0));
		mappings.put(Blocks.red_mushroom, AIR);
		mappings.put(Blocks.brown_mushroom, AIR);
		mappings.put(Blocks.double_plant, AIR);
		mappings.put(Blocks.tallgrass, AIR);
		register(BiomeGenBase.beach, mappings);
		register(BiomeGenBase.coldBeach, mappings);

		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.dirt, new ThemeBlock(Blocks.stone, 0));
		mappings.put(Blocks.grass, new ThemeBlock(Blocks.stone, 0));
		mappings.put(Blocks.red_flower, new ThemeBlock(Blocks.red_mushroom, 0));
		mappings.put(Blocks.yellow_flower, new ThemeBlock(Blocks.brown_mushroom, 0));
		mappings.put(Blocks.double_plant, AIR);
		mappings.put(Blocks.tallgrass, AIR);
		register(BiomeGenBase.stoneBeach, mappings);

		// Birch Forest
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log, 2));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log, 2));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.planks, 2));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.birch_stairs, KEEP_META));
		mappings.put(Blocks.oak_stairs, new ThemeBlock(Blocks.birch_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.birch_stairs, KEEP_META));
		mappings.put(Blocks.jungle_stairs, new ThemeBlock(Blocks.birch_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.birch_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 2));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 2));
		register(BiomeGenBase.birchForest, mappings);
		register(BiomeGenBase.birchForestHills, mappings);

		// Desert
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.sandstone, 0));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.sandstone, 0));
		mappings.put(Blocks.cobblestone, new ThemeBlock(Blocks.sandstone, 0));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.sandstone, 2));
		mappings.put(Blocks.oak_stairs, new ThemeBlock(Blocks.sandstone_stairs, KEEP_META));
		mappings.put(Blocks.stone_stairs, new ThemeBlock(Blocks.sandstone_stairs, KEEP_META));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.sandstone_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.sandstone_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.sandstone_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.sandstone_stairs, KEEP_META));
		mappings.put(Blocks.gravel, new ThemeBlock(Blocks.sandstone, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.stone_slab, 1));
		mappings.put(Blocks.stone_slab, new ThemeBlock(Blocks.stone_slab, 1));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.sandstone, 0));
		mappings.put(Blocks.double_stone_slab, new ThemeBlock(Blocks.sandstone, 0));
		mappings.put(Blocks.dirt, new ThemeBlock(Blocks.sand, 0));
		mappings.put(Blocks.grass, new ThemeBlock(Blocks.sand, 0));
		mappings.put(Blocks.red_flower, new ThemeBlock(Blocks.deadbush, 0));
		mappings.put(Blocks.yellow_flower, new ThemeBlock(Blocks.deadbush, 0));
		mappings.put(Blocks.red_mushroom, AIR);
		mappings.put(Blocks.brown_mushroom, AIR);
		mappings.put(Blocks.double_plant, AIR);
		mappings.put(Blocks.tallgrass, AIR);
		register(BiomeGenBase.desert, mappings);
		register(BiomeGenBase.desertHills, mappings);

		// Forest
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log, 0));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log, 0));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.planks, 0));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.jungle_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 0));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 0));
		register(BiomeGenBase.forest, mappings);
		register(BiomeGenBase.forestHills, mappings);

		// Jungle
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log, 3));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log, 3));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.planks, 3));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.jungle_stairs, KEEP_META));
		mappings.put(Blocks.oak_stairs, new ThemeBlock(Blocks.jungle_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.jungle_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.jungle_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.jungle_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 3));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 3));
		register(BiomeGenBase.jungle, mappings);
		register(BiomeGenBase.jungleEdge, mappings);
		register(BiomeGenBase.jungleHills, mappings);

		// Roofed Forest (Dark Oak)
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log2, 1));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log2, 1));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.planks, 5));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.dark_oak_stairs, KEEP_META));
		mappings.put(Blocks.oak_stairs, new ThemeBlock(Blocks.dark_oak_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.dark_oak_stairs, KEEP_META));
		mappings.put(Blocks.jungle_stairs, new ThemeBlock(Blocks.dark_oak_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.dark_oak_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 5));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 5));
		register(BiomeGenBase.roofedForest, mappings);

		// Savanna
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log2, 0));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log2, 0));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.planks, 4));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.acacia_stairs, KEEP_META));
		mappings.put(Blocks.oak_stairs, new ThemeBlock(Blocks.acacia_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.acacia_stairs, KEEP_META));
		mappings.put(Blocks.jungle_stairs, new ThemeBlock(Blocks.acacia_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.acacia_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 4));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 4));
		register(BiomeGenBase.savanna, mappings);
		register(BiomeGenBase.savannaPlateau, mappings);

		// Taiga
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log, 1));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log, 1));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.planks, 1));
		mappings.put(Blocks.oak_stairs, new ThemeBlock(Blocks.spruce_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.spruce_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.spruce_stairs, KEEP_META));
		mappings.put(Blocks.jungle_stairs, new ThemeBlock(Blocks.spruce_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.spruce_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 1));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 1));
		register(BiomeGenBase.coldTaiga, mappings);
		register(BiomeGenBase.coldTaigaHills, mappings);
		register(BiomeGenBase.megaTaiga, mappings);
		register(BiomeGenBase.megaTaigaHills, mappings);
		register(BiomeGenBase.taiga, mappings);
		register(BiomeGenBase.taigaHills, mappings);

		// Mushroom
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.grass, new ThemeBlock(Blocks.mycelium));
		mappings.put(Blocks.log, new ThemeBlock(Blocks.red_mushroom_block));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.red_mushroom_block));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.brown_mushroom_block));
		mappings.put(Blocks.red_flower, new ThemeBlock(Blocks.red_mushroom));
		mappings.put(Blocks.yellow_flower, new ThemeBlock(Blocks.brown_mushroom, 0));
		mappings.put(Blocks.double_plant, AIR);
		mappings.put(Blocks.tallgrass, AIR);
		register(BiomeGenBase.mushroomIsland, mappings);
		register(BiomeGenBase.mushroomIslandShore, mappings);

		// Swamp
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log, 0));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log, 0));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.planks, 0));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.jungle_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 0));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 0));
		mappings.put(Blocks.red_flower, new ThemeBlock(Blocks.red_flower, 1));
		mappings.put(Blocks.yellow_flower, new ThemeBlock(Blocks.brown_mushroom, 0));
		register(BiomeGenBase.swampland, mappings);

		// Mesa
		mappings = new IdentityHashMap<Block, ThemeBlock>();
		mappings.put(Blocks.dirt, new ThemeBlock(Blocks.sand, 1));
		mappings.put(Blocks.grass, new ThemeBlock(Blocks.sand, 1));
		mappings.put(Blocks.log, new ThemeBlock(Blocks.log, 0));
		mappings.put(Blocks.log2, new ThemeBlock(Blocks.log, 0));
		mappings.put(Blocks.planks, new ThemeBlock(Blocks.hardened_clay));
		mappings.put(Blocks.spruce_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.birch_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.dark_oak_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.jungle_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.acacia_stairs, new ThemeBlock(Blocks.oak_stairs, KEEP_META));
		mappings.put(Blocks.wooden_slab, new ThemeBlock(Blocks.wooden_slab, 0));
		mappings.put(Blocks.double_wooden_slab, new ThemeBlock(Blocks.double_wooden_slab, 0));
		mappings.put(Blocks.red_flower, new ThemeBlock(Blocks.deadbush));
		mappings.put(Blocks.yellow_flower, new ThemeBlock(Blocks.deadbush));
		mappings.put(Blocks.red_mushroom, AIR);
		mappings.put(Blocks.brown_mushroom, AIR);
		mappings.put(Blocks.double_plant, AIR);
		mappings.put(Blocks.tallgrass, AIR);
		register(BiomeGenBase.mesa, mappings);
		register(BiomeGenBase.mesaPlateau, mappings);

		// Hook for block replacement
		MinecraftForge.TERRAIN_GEN_BUS.register(new BlockThemes());
	}

	// Forge Event listener for block replacement. Low priority because
	// we give other mods a chance to replace before we intercept. This
	// routine is most commonly called during village generation, though
	// there isn't a restriction saying this is the only time a replace
	// request can be made.
	@SubscribeEvent(priority = EventPriority.LOW)
	public void blockReplaceEvent(final BiomeEvent.GetVillageBlockID event) {
		if (event.getResult() == Result.DENY)
			return;
		final Map<Block, ThemeBlock> replacements = themes.get(event.biome);
		if (replacements == null)
			return;

		// Crack the block portion
		final IBlockState original = event.original;
		final ThemeBlock theme = replacements.get(original.getBlock());
		if (theme == null)
			return;

		final Block newBlock = theme.getBlock();

		// Handle the meta
		int newMeta = theme.getMeta();
		if (newMeta != KEEP_META) {
			// Preserve slab orientation
			if (theme.isSlab())
				newMeta |= (theme.getMeta() & 8);
			// Preserve log orientation
			else if (theme.isLog())
				newMeta |= (theme.getMeta() & 12);
		} else {
			newMeta = original.getBlock().getMetaFromState(original);
		}

		event.replacement = newBlock.getStateFromMeta(newMeta);
		event.setResult(Result.DENY);
	}
}
