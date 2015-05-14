package org.blockartistry.mod.Restructured.assets;

import org.blockartistry.mod.Restructured.schematica.ISchematic;

public final class SchematicProperties {
	
	public String name;
	public boolean isVillageStructure;
	public boolean isWorldGenStructure;
	public int weight;
	public int limit;
	public int groundOffset;
	public ISchematic schematic;
	public boolean suppressFire;
	public int villagerCount;
	public int villagerProfession;

	public SchematicProperties() {
		super();
	}

	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("[%s] (isVillage: %s; isWorld: %s)", name, Boolean.valueOf(isVillageStructure), Boolean.valueOf(isWorldGenStructure)));
		builder.append(String.format(" (weight: %d; limit: %d)", weight, limit));
		builder.append(String.format(" (offset: %d; noFire: %s)", groundOffset, Boolean.valueOf(suppressFire)));
		builder.append(String.format(" (villagers: %d; profession: %d)", villagerCount, villagerProfession));
		
		return builder.toString();
	}
}