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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.Configuration;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.Restructured;
import org.blockartistry.mod.Restructured.component.SchematicStructureCreationHandler;
import org.blockartistry.mod.Restructured.schematica.SchematicFormat;
import org.blockartistry.mod.Restructured.util.WeightTable;

import com.google.common.base.Preconditions;

public final class Assets {

	static List<SchematicProperties> schematicList = null;
	
	static WeightTable<SchematicWeightItem> villageSchematics = new WeightTable<SchematicWeightItem>();
	static WeightTable<SchematicWeightItem> worldSchematics = new WeightTable<SchematicWeightItem>();

	static final boolean DEFAULT_IS_WORLD = false;
	static final boolean DEFAULT_IS_VILLAGE = true;
	static final boolean DEFAULT_SUPPRESS_FIRE = true;
	static final int DEFAULT_WEIGHT = 10;
	static final int DEFAULT_LIMIT = 1;
	static final int DEFAULT_OFFSET = 1;
	static final int DEFAULT_VILLAGER_COUNT = 1;
	static final int DEFAULT_VILLAGER_PROFESSION = -1;

	static final String CONFIG_STRUCTURES = "structures";
	static final String OPTION_IS_WORLD = "includeInWorldGen";
	static final String OPTION_IS_VILLAGE = "includeInVillageGen";
	static final String OPTION_SUPPRESS_FIRE = "suppressFire";
	static final String OPTION_WEIGHT = "weight";
	static final String OPTION_LIMIT = "limit";
	static final String OPTION_VILLAGER_COUNT = "villagerCount";
	static final String OPTION_VILLAGER_PROFESSION = "villagerProfession";
	static final String OPTION_OFFSET = "groundOffset";

	private static final String SCHEMATIC_RESOURCE_PATH = "schematics";
	private static final String SCHEMATIC_RESOURCE_EXTENSION = ".schematic";

	private static File accessPath = null;
	private static Configuration config = null;

	static {

		accessPath = new File(Restructured.dataDirectory(),
				SCHEMATIC_RESOURCE_PATH);
		if (!accessPath.exists())
			accessPath.mkdirs();

		config = new Configuration(new File(accessPath, "schematics.cfg"));
	}

	public static List<SchematicProperties> getSchematicPropertyList() {

		if (schematicList != null)
			return schematicList;

		schematicList = new ArrayList<SchematicProperties>();

		for (File f : accessPath.listFiles()) {
			if (f.isFile()
					&& f.getName().endsWith(SCHEMATIC_RESOURCE_EXTENSION)) {
				
				SchematicProperties props = new SchematicProperties();
				
				props.name = StringUtils.removeEnd(f.getName(),
						SCHEMATIC_RESOURCE_EXTENSION);
				String category = CONFIG_STRUCTURES + "." + props.name;

				props.isVillageStructure = config.getBoolean(OPTION_IS_VILLAGE,
						category, DEFAULT_IS_VILLAGE,
						"Include structure in village generation");
				props.isWorldGenStructure = config.getBoolean(OPTION_IS_WORLD, category,
						DEFAULT_IS_WORLD,
						"Include structure in world generation");
				props.suppressFire = config.getBoolean(OPTION_SUPPRESS_FIRE, category,
						DEFAULT_SUPPRESS_FIRE,
						"Suppress fire sources when generating");
				props.villagerCount = config.getInt(OPTION_VILLAGER_COUNT, category,
						DEFAULT_VILLAGER_COUNT, -1, Integer.MAX_VALUE,
						"Number of villagers to spawn for the structure (-1 random)");
				props.villagerProfession = config.getInt(OPTION_VILLAGER_PROFESSION, category,
						DEFAULT_VILLAGER_PROFESSION, -1, 4,
						"Villager profession: -1 random, 0 farmer, 1 librarian, 2 priest, 3 smith, 4 butcher");
				props.weight = config.getInt(OPTION_WEIGHT, category,
						DEFAULT_WEIGHT, 0, Integer.MAX_VALUE,
						"Relative weight for selection");
				props.limit = config
						.getInt(OPTION_LIMIT, category, DEFAULT_LIMIT, 0,
								Integer.MAX_VALUE,
								"Maximum number of this type of structure to have in a village");
				props.groundOffset = config
						.getInt(OPTION_OFFSET, category, DEFAULT_OFFSET, 0,
								Integer.MAX_VALUE,
								"The number of blocks below ground the structure extends");
				

 				try {
					InputStream stream = Assets.getSchematicFile(props.name);
					props.schematic = SchematicFormat.readFromStream(stream);
					stream.close();
				} catch (IOException e) {
					;
				}

				if(props.schematic != null)
					schematicList.add(props);
			}
		}
		
		config.save();

		return schematicList;
	}
	
	public static SchematicProperties getProperties(String schematic) {
		for(SchematicProperties p: getSchematicPropertyList())
			if(p.name.equals(schematic))
				return p;
		return null;
	}

	public static Configuration getSchematicConfig() {
		return config;
	}

	public static InputStream getSchematicFile(String name) {
		Preconditions.checkNotNull(name);

		InputStream result = null;

		try {
			result = new FileInputStream(new File(accessPath, name
					+ SCHEMATIC_RESOURCE_EXTENSION));
		} catch (FileNotFoundException e) {
			ModLog.warn("Unable to locate schematic [%s]", name);
			e.printStackTrace();
		}

		return result;
	}
	
	public static int villageStructureCount() {
		return villageSchematics.size();
	}
	
	public static int villageStructureTotalWeight() {
		return villageSchematics.getTotalWeight();
	}
	
	public static int worldStructureCount() {
		return worldSchematics.size();
	}
	
	public static SchematicProperties getNextVillageStructure() {
		return villageSchematics.next().properties;
	}
	
	public static SchematicProperties getNextWorldStructure() {
		return worldSchematics.next().properties;
	}
	
	public static void initialize() {
		
		for(SchematicProperties p: getSchematicPropertyList()) {
			
			if(p.isVillageStructure)
				villageSchematics.add(new SchematicWeightItem(p));
			
			if(p.isWorldGenStructure)
				worldSchematics.add(new SchematicWeightItem(p));
			
			if(p.isVillageStructure || p.isWorldGenStructure)
				ModLog.info(p.toString());
		}
		
		if(villageStructureCount() > 0)
			new SchematicStructureCreationHandler();
	}
}
