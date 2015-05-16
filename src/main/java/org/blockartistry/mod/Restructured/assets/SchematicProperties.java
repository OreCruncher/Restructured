package org.blockartistry.mod.Restructured.assets;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import org.blockartistry.mod.Restructured.schematica.ISchematic;
import org.blockartistry.mod.Restructured.util.ElementRule;

public final class SchematicProperties {

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