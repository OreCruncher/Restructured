package org.blockartistry.mod.Restructured.assets;

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

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append(String.format("[%s] (village: %d; world: %d)", name, villageWeight, worldWeight));
		builder.append(String.format(" (offset: %d; noFire: %s)", groundOffset,
				Boolean.valueOf(suppressFire)));
		builder.append(String.format(" (villagers: %d; profession: %d)",
				villagerCount, villagerProfession));
		builder.append(String.format(" (chest: %s, count: %d)",
				(chestContents == null) ? "<None>" : chestContents,
				chestContentsCount));
		builder.append(String.format(" (spawner: %d)", spawnerEnableChance));

		return builder.toString();
	}
}