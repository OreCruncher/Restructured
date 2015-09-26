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
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.ModOptions;
import org.blockartistry.mod.Restructured.Restructured;
import org.blockartistry.mod.Restructured.component.SchematicStructureCreationHandler;
import org.blockartistry.mod.Restructured.schematica.SchematicFormat;
import org.blockartistry.mod.Restructured.util.ElementRule;
import org.blockartistry.mod.Restructured.util.ElementRule.Rule;
import org.blockartistry.mod.Restructured.util.ItemStackHelper;
import org.blockartistry.mod.Restructured.util.MyUtils;
import org.blockartistry.mod.Restructured.util.WeightTable;
import org.blockartistry.mod.Restructured.world.SchematicWorldGenHandler;

import com.google.common.base.Preconditions;

import cpw.mods.fml.common.registry.GameData;

public final class Assets {

	static List<SchematicProperties> schematicList = null;
	static List<ChestGenHooks> chestHooks = null;
	static List<ItemSeeds> seeds = null;

	static WeightTable<SchematicWeightItem> villageSchematics = new WeightTable<SchematicWeightItem>();
	static WeightTable<SchematicWeightItem> worldSchematics = new WeightTable<SchematicWeightItem>();

	static final boolean DEFAULT_IS_WORLD = false;
	static final boolean DEFAULT_IS_VILLAGE = true;
	static final String DEFAULT_OPTIONS = "suppressFire;suppressEggs";
	
	static final int DEFAULT_VILLAGE_WEIGHT = 10;
	static final int DEFAULT_WORLD_WEIGHT = 0;
	static final int DEFAULT_LIMIT = 1;
	static final int DEFAULT_OFFSET = 1;
	static final int DEFAULT_VILLAGER_COUNT = 1;
	static final int DEFAULT_VILLAGER_PROFESSION = -1;
	static final String DEFAULT_CHEST_CONTENTS = "";
	static final int DEFAULT_CHEST_CONTENTS_COUNT = 1;
	static final int DEFAULT_SPAWNER_ENABLE_CHANCE = 15;
	static final boolean DEFAULT_BIOME_LIST_TYPE = true;
	static final boolean DEFAULT_DIMENSION_LIST_TYPE = true;

	static final int[] DEFAULT_BIOME_LIST = new int[] {
			BiomeGenBase.deepOcean.biomeID, BiomeGenBase.frozenOcean.biomeID,
			BiomeGenBase.frozenRiver.biomeID, BiomeGenBase.ocean.biomeID,
			BiomeGenBase.river.biomeID, BiomeGenBase.swampland.biomeID,
			BiomeGenBase.beach.biomeID, };

	static final int[] DEFAULT_DIMENSION_LIST = new int[] { 1, -1 };

	static final String CONFIG_STRUCTURES = "structures";
	static final String OPTION_IS_WORLD = "includeInWorldGen";
	static final String OPTION_IS_VILLAGE = "includeInVillageGen";
	static final String OPTION_OPTIONS = "generationOptions";
	static final String OPTION_OPTIONS_SUPPRESS_FIRE = "suppressFire";
	static final String OPTION_OPTIONS_SUPPRESS_EGGS = "suppressEggs";
	static final String OPTION_OPTIONS_RANDOM_CROPS = "randomCrops";
	
	static final String OPTION_VILLAGE_WEIGHT = "villageWeight";
	static final String OPTION_WORLD_WEIGHT = "worldWeight";
	static final String OPTION_LIMIT = "limit";
	static final String OPTION_VILLAGER_COUNT = "villagerCount";
	static final String OPTION_VILLAGER_PROFESSION = "villagerProfession";
	static final String OPTION_OFFSET = "groundOffset";
	static final String OPTION_CHEST_CONTENTS = "chestContents";
	static final String OPTION_CHEST_CONTENTS_COUNT = "chestContentsCount";
	static final String OPTION_SPAWNER_ENABLE_CHANCE = "spawnerEnableChance";
	static final String OPTION_BIOME_LIST_TYPE = "biomeListAsBlacklist";
	static final String OPTION_DIMENSION_LIST_TYPE = "dimensionListAsBlacklist";
	static final String OPTION_BIOME_LIST = "biomeList";
	static final String OPTION_DIMENSION_LIST = "dimensionList";

	static final String CONFIG_CHESTS = "chests";

	private static final String SCHEMATIC_RESOURCE_PATH = "schematics";
	private static final String SCHEMATIC_RESOURCE_EXTENSION = ".schematic";

	private static File accessPath = null;
	private static Configuration config = null;
	private static Configuration chests = null;

	static {

		accessPath = new File(Restructured.dataDirectory(),
				SCHEMATIC_RESOURCE_PATH);
		if (!accessPath.exists())
			accessPath.mkdirs();

		config = new Configuration(new File(accessPath, "schematics.cfg"));
		chests = new Configuration(new File(accessPath, "chests.cfg"));
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

				props.villageWeight = config
						.getInt(OPTION_VILLAGE_WEIGHT, category,
								DEFAULT_VILLAGE_WEIGHT, 0, Integer.MAX_VALUE,
								"Relative selection weight for village structure generation");

				props.worldWeight = config.getInt(OPTION_WORLD_WEIGHT,
						category, DEFAULT_WORLD_WEIGHT, 0, Integer.MAX_VALUE,
						"Relative selection weight for world generation");

				props.villagerCount = config
						.getInt(OPTION_VILLAGER_COUNT, category,
								DEFAULT_VILLAGER_COUNT, -1, Integer.MAX_VALUE,
								"Number of villagers to spawn for the structure (-1 random)");

				props.villagerProfession = config
						.getInt(OPTION_VILLAGER_PROFESSION,
								category,
								DEFAULT_VILLAGER_PROFESSION,
								-1,
								4,
								"Villager profession: -1 random, 0 farmer, 1 librarian, 2 priest, 3 smith, 4 butcher");

				props.limit = config
						.getInt(OPTION_LIMIT, category, DEFAULT_LIMIT, 0,
								Integer.MAX_VALUE,
								"Maximum number of this type of structure to have in a village");

				props.groundOffset = config
						.getInt(OPTION_OFFSET, category, DEFAULT_OFFSET, 0,
								Integer.MAX_VALUE,
								"The number of blocks below ground the structure extends");

				props.chestContents = config
						.getString(OPTION_CHEST_CONTENTS, category,
								DEFAULT_CHEST_CONTENTS,
								"What chest generation hook to use when filling chests");

				props.chestContentsCount = config
						.getInt(OPTION_CHEST_CONTENTS_COUNT, category,
								DEFAULT_CHEST_CONTENTS_COUNT, 0,
								Integer.MAX_VALUE,
								"The number of stacks to pull from the generation table");

				props.spawnerEnableChance = config.getInt(
						OPTION_SPAWNER_ENABLE_CHANCE, category,
						DEFAULT_SPAWNER_ENABLE_CHANCE, 0, 100,
						"Chance that a spawner will be preserved when placed");

				boolean asBlackList = config.getBoolean(OPTION_BIOME_LIST_TYPE,
						category, DEFAULT_BIOME_LIST_TYPE,
						"Treat the biome list as a blacklist vs. whitelist");
				String def = MyUtils.join(";", DEFAULT_BIOME_LIST);
				String list = config.getString(OPTION_BIOME_LIST, category,
						def, "List of biome IDs");

				try {
					props.biomes = new ElementRule(
							asBlackList ? Rule.MUST_NOT_BE_IN : Rule.MUST_BE_IN,
							MyUtils.split(";", list));
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				asBlackList = config
						.getBoolean(OPTION_DIMENSION_LIST_TYPE, category,
								DEFAULT_DIMENSION_LIST_TYPE,
								"Treat the dimension list as a blacklist vs. whitelist");
				def = MyUtils.join(";", DEFAULT_DIMENSION_LIST);
				list = config.getString(OPTION_DIMENSION_LIST, category, def,
						"List of dimension IDs");

				String options = config.getString(OPTION_OPTIONS, category, DEFAULT_OPTIONS, "Options for generation");
				props.suppressFire = options.contains(OPTION_OPTIONS_SUPPRESS_FIRE);
				props.suppressMonsterEgg = options.contains(OPTION_OPTIONS_SUPPRESS_EGGS);
				props.randomizeCrops = options.contains(OPTION_OPTIONS_RANDOM_CROPS);
				
				try {
					props.dimensions = new ElementRule(
							asBlackList ? Rule.MUST_NOT_BE_IN : Rule.MUST_BE_IN,
							MyUtils.split(";", list));
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				try {
					InputStream stream = Assets.getSchematicFile(props.name);
					props.schematic = SchematicFormat.readFromStream(stream);
					stream.close();
				} catch (IOException e) {
					;
				}

				if (props.schematic != null)
					schematicList.add(props);
			}
		}

		return schematicList;
	}

	public static SchematicProperties getProperties(String schematic) {
		for (SchematicProperties p : getSchematicPropertyList())
			if (p.name.equals(schematic))
				return p;
		return null;
	}

	public static List<ChestGenHooks> getChestGenerationHooks() {

		if (chestHooks != null)
			return chestHooks;

		chestHooks = new ArrayList<ChestGenHooks>();
		ConfigCategory c = chests.getCategory(CONFIG_CHESTS);

		for (ConfigCategory p : c.getChildren()) {
			String chestHookName = p.getName();

			for (Entry<String, Property> item : p.getValues().entrySet()) {

				ItemStack stack = ItemStackHelper.getItemStack(item.getKey());
				if (stack == null
						|| stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
					ModLog.warn("Invalid item: %s", item.getKey());
					continue;
				}

				try {

					String values = item.getValue().getString();
					String[] parms = values.split(",");
					if (parms.length == 3) {

						int min = Integer.valueOf(parms[0]);
						int max = Integer.valueOf(parms[1]);
						int weight = Integer.valueOf(parms[2]);

						ChestGenHooks.addItem(chestHookName,
								new WeightedRandomChestContent(stack, min, max,
										weight));

					} else {
						ModLog.warn(
								"Invalid number of values in parameter string: %s",
								values);
					}

				} catch (Throwable e) {
					ModLog.error("Unable to parse chest entry", e);
				}
			}

			chestHooks.add(ChestGenHooks.getInfo(chestHookName));
		}

		// No longer need it - make sure it goes away
		chests = null;

		return chestHooks;
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

	public static boolean areSchematicsInstalled() {
		return villageSchematics.size() != 0 || worldSchematics.size() != 0;
	}
	
	public static int villageStructureCount() {
		return villageSchematics.size();
	}

	public static int villageStructureTotalWeight() {
		return villageSchematics.getTotalWeight();
	}
	
	public static int villageStructureTotalLimit() {
		int limit = 0;
		
		for(SchematicWeightItem e: villageSchematics.getEntries())
			limit += e.properties.limit;
		
		return limit;
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
	
	public static WeightTable<SchematicWeightItem> getTableForVillageGen()
	{
		WeightTable<SchematicWeightItem> table = new WeightTable<SchematicWeightItem>();
		for (SchematicWeightItem e : villageSchematics.getEntries()) {
			try {
				table.add((SchematicWeightItem)e.clone());
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
		}

		return table;
	}

	public static WeightTable<SchematicWeightItem> getTableForWorldGen(
			int dimId, BiomeGenBase biome) {

		WeightTable<SchematicWeightItem> table = new WeightTable<SchematicWeightItem>();
		for (SchematicWeightItem e : worldSchematics.getEntries()) {
			SchematicProperties p = e.properties;
			if (p.dimensions.isOk(dimId) && p.biomes.isOk(biome.biomeID))
				try {
					table.add((SchematicWeightItem)e.clone());
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
		}

		return table;
	}
	
	public static List<ItemSeeds> getSeeds() {
		
		if(seeds != null)
			return seeds;
		
		seeds = new ArrayList<ItemSeeds>();
		for(Item i: GameData.getItemRegistry().typeSafeIterable())
			if((i instanceof ItemSeeds) && i != Items.pumpkin_seeds && i != Items.melon_seeds)
				seeds.add((ItemSeeds)i);
		
		return seeds;
	}
	
	protected static void dumpBiomeList() {

		ModLog.info("Detected biomes:");

		for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
			if (b != null)
				ModLog.info("Biome [%s] id=%d", b.biomeName, b.biomeID);
	}

	public static void initialize() {

		for (SchematicProperties p : getSchematicPropertyList()) {

			if (p.villageWeight > 0)
				villageSchematics.add(new SchematicWeightItem(p, true));

			if (p.worldWeight > 0)
				worldSchematics.add(new SchematicWeightItem(p, false));

			if (p.villageWeight > 0 || p.worldWeight > 0) {
				ModLog.info(p.toString());

				if (ModOptions.getEnableDebugLogging()) {
					Map<Block, Integer> analysis = p.analyze();
					for (Entry<Block, Integer> e : analysis.entrySet()) {
						ModLog.info("Block: [%s], count=%d", e.getKey()
								.getLocalizedName(), e.getValue().intValue());
					}
				}
			}
		}

		// Just call once - process will register the hook info
		// during load.
		getChestGenerationHooks();
		config.save();

		// Create our handlers
		if (villageStructureCount() > 0) {
			ModLog.info("Registering village structure handler");
			new SchematicStructureCreationHandler();
		}

		if (worldStructureCount() > 0) {
			ModLog.info("Regsitering world generation handler");
			new SchematicWorldGenHandler();
		}

		dumpBiomeList();
	}
}
