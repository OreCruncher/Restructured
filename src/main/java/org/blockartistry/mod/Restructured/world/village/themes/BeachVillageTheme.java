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

public class BeachVillageTheme  extends VillageTheme {

	public static void initialize() {
		
		HashMap<Block, Integer> mappings = new HashMap<Block, Integer>();

		mappings.put(Blocks.dirt, encode(Blocks.sand, 0));
		mappings.put(Blocks.grass, encode(Blocks.sand, 0));
		mappings.put(Blocks.red_flower, encode(Blocks.deadbush, 0));
		mappings.put(Blocks.yellow_flower, encode(Blocks.deadbush, 0));
		mappings.put(Blocks.red_mushroom, encode(Blocks.air, 0));
		mappings.put(Blocks.brown_mushroom, encode(Blocks.air, 0));
		mappings.put(Blocks.double_plant, encode(Blocks.air, 0));
		mappings.put(Blocks.tallgrass, encode(Blocks.air, 0));
		
		register(BiomeGenBase.beach, new BeachVillageTheme(BiomeGenBase.beach, mappings));
		register(BiomeGenBase.coldBeach, new BeachVillageTheme(BiomeGenBase.coldBeach, mappings));

		mappings = new HashMap<Block, Integer>();

		mappings.put(Blocks.dirt, encode(Blocks.stone, 0));
		mappings.put(Blocks.grass, encode(Blocks.stone, 0));
		mappings.put(Blocks.red_flower, encode(Blocks.red_mushroom, 0));
		mappings.put(Blocks.yellow_flower, encode(Blocks.brown_mushroom, 0));
		mappings.put(Blocks.double_plant, encode(Blocks.air, 0));
		mappings.put(Blocks.tallgrass, encode(Blocks.air, 0));
		register(BiomeGenBase.stoneBeach, new BeachVillageTheme(BiomeGenBase.stoneBeach, mappings));
	}
	
	protected BeachVillageTheme(BiomeGenBase biome, Map<Block, Integer> mapping) {
		super(biome, mapping);
	}
}
