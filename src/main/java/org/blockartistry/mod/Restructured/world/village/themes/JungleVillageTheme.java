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

package org.blockartistry.mod.Restructured.world.village.themes;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

public class JungleVillageTheme extends VillageTheme {

	public static void initialize() {
		
		final HashMap<Block, Integer> mappings = new HashMap<Block, Integer>();

		// Replace with jungle
		mappings.put(Blocks.log, encode(Blocks.log, 3));
		mappings.put(Blocks.log2, encode(Blocks.log, 3));
		mappings.put(Blocks.planks, encode(Blocks.planks, 3));
		mappings.put(Blocks.spruce_stairs, encode(Blocks.jungle_stairs, -1));
		mappings.put(Blocks.oak_stairs, encode(Blocks.jungle_stairs, -1));
		mappings.put(Blocks.dark_oak_stairs, encode(Blocks.jungle_stairs, -1));
		mappings.put(Blocks.birch_stairs, encode(Blocks.jungle_stairs, -1));
		mappings.put(Blocks.acacia_stairs, encode(Blocks.jungle_stairs, -1));
		mappings.put(Blocks.wooden_slab, encode(Blocks.wooden_slab, 3));
		mappings.put(Blocks.double_wooden_slab, encode(Blocks.double_wooden_slab, 3));
		
		register(BiomeGenBase.jungle, new JungleVillageTheme(BiomeGenBase.jungle, mappings));
		register(BiomeGenBase.jungleEdge, new JungleVillageTheme(BiomeGenBase.jungleEdge, mappings));
		register(BiomeGenBase.jungleHills, new JungleVillageTheme(BiomeGenBase.jungleHills, mappings));
	}
	
	protected JungleVillageTheme(BiomeGenBase biome, Map<Block, Integer> mapping) {
		super(biome, mapping);
	}

}
