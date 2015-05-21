/*
 * This file is part of Restructured, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 * Copyright (c) contributors
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

package org.blockartistry.mod.Restructured.assets;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import org.blockartistry.mod.Restructured.schematica.ISchematic;
import org.blockartistry.mod.Restructured.util.ElementRule;

public final class SchematicProperties implements Cloneable {

	public String name;
	public int villageWeight;
	public int worldWeight;
	public int limit;
	public int groundOffset;
	public ISchematic schematic;
	public boolean suppressFire;
	public boolean suppressMonsterEgg;
	public int villagerCount;
	public int villagerProfession;
	public String chestContents;
	public int chestContentsCount;
	public int spawnerEnableChance;
	public ElementRule dimensions;
	public ElementRule biomes;

	public SchematicProperties() {
		super();
	}

	public Map<Block, Integer> analyze() {

		HashMap<Block, Integer> result = new HashMap<Block, Integer>();

		int upperX = schematic.getWidth();
		int upperY = schematic.getHeight();
		int upperZ = schematic.getLength();

		for (int x = 0; x < upperX; x++)
			for (int y = 0; y < upperY; y++)
				for (int z = 0; z < upperZ; z++) {
					Block block = schematic.getBlock(x, y, z);
					if (block != Blocks.air) {
						Integer v = result.get(block);
						if (v == null)
							v = new Integer(1);
						else
							v = new Integer(v.intValue() + 1);
						result.put(block, v);
					}
				}

		return result;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append(String.format("[%s] (village: %d; world: %d)", name,
				villageWeight, worldWeight));
		builder.append(String.format(" (offset: %d; noFire: %s; noEgg: %s)", groundOffset,
				Boolean.valueOf(suppressFire), Boolean.valueOf(suppressMonsterEgg)));
		builder.append(String.format(" (villagers: %d; profession: %d)",
				villagerCount, villagerProfession));
		builder.append(String.format(" (chest: %s, count: %d)",
				(chestContents == null) ? "<None>" : chestContents,
				chestContentsCount));
		builder.append(String.format(" (spawner: %d)", spawnerEnableChance));

		return builder.toString();
	}
}