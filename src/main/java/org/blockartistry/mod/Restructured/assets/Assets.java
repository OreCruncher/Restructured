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
import org.blockartistry.mod.Restructured.util.ItemStackHelper;
import org.blockartistry.mod.Restructured.util.StreamUtils;
import org.blockartistry.mod.Restructured.util.WeightTable;
import org.blockartistry.mod.Restructured.world.SchematicWorldGenHandler;

import cpw.mods.fml.common.registry.GameData;

public final class Assets {

	private static final String STANDARD_PACK = "StandardPack.zip";

	private static final List<SchematicProperties> schematicList = new ArrayList<SchematicProperties>();
	private static List<ChestGenHooks> chestHooks = null;
	private static List<ItemSeeds> seeds = null;

	// Editable configuration
	private static final File accessPath = Restructured.dataDirectory();
	private static Configuration config = new Configuration(new File(accessPath, "schematics.cfg"));
	private static Configuration chests = new Configuration(new File(accessPath, "chests.cfg"));

	private static final WeightTable<SchematicWeightItem> villageSchematics = new WeightTable<SchematicWeightItem>();
	private static final WeightTable<SchematicWeightItem> worldSchematics = new WeightTable<SchematicWeightItem>();

	static {
		ModLog.info("Schematic ZIPs present: %s", Boolean.toString(ZipProcessor.areZipsPresent(accessPath)));
		ModLog.info("Schematic Files present: %s", Boolean.toString(ZipProcessor.areSchematicsPresent(accessPath)));
		// If there are no zips or schematics present extract the standard
		// archive into the folder.
		if (!ZipProcessor.areZipsPresent(accessPath) && !ZipProcessor.areSchematicsPresent(accessPath)) {
			ModLog.info("Extracting %s to configuration directory", STANDARD_PACK);
			try {
				final InputStream input = ClassLoader.getSystemResourceAsStream("assets/recycling/" + STANDARD_PACK);
				if (input != null) {
					StreamUtils.copy(input, new File(accessPath, STANDARD_PACK));
					input.close();
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static SchematicProperties getProperties(final String schematic) {
		for (final SchematicProperties p : schematicList)
			if (p.name.equals(schematic))
				return p;
		return null;
	}

	private static boolean isContainerNode(final ConfigCategory c) {
		return !c.getChildren().isEmpty();
	}
	
	private static void processEntry(final ConfigCategory p, final ConfigCategory cc, final List<ChestGenHooks> list) {
		String chestHookName = null;
		if(p == null || cc.getName().startsWith("^"))
			chestHookName = StringUtils.removeStart(cc.getName(), "^");
		else
			chestHookName = p.getName() + "." + cc.getName();
		
		for (final Entry<String, Property> item : cc.getValues().entrySet()) {

			final ItemStack stack = ItemStackHelper.getItemStack(item.getKey());
			if (stack == null || stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
				ModLog.warn("Invalid item: %s", item.getKey());
				continue;
			}

			try {

				final String values = item.getValue().getString();
				final String[] parms = values.split(",");
				if (parms.length == 3) {

					final int min = Integer.valueOf(parms[0]);
					final int max = Integer.valueOf(parms[1]);
					final int weight = Integer.valueOf(parms[2]);

					ChestGenHooks.addItem(chestHookName,
							new WeightedRandomChestContent(stack, min, max, weight));

				} else {
					ModLog.warn("Invalid number of values in parameter string: %s", values);
				}

			} catch (final Exception e) {
				ModLog.error("Unable to parse chest entry", e);
			}
		}
		list.add(ChestGenHooks.getInfo(chestHookName));
		ModLog.info("Loaded chest loot table '%s'", chestHookName);
	}
	
	private static List<ChestGenHooks> getChestGenerationHooks() {

		if (chestHooks != null)
			return chestHooks;

		chestHooks = new ArrayList<ChestGenHooks>();
		final ConfigCategory c = chests.getCategory(ConfigProcessor.CONFIG_CHESTS);

		for (final ConfigCategory p : c.getChildren()) {
			if(!isContainerNode(p)) {
				processEntry(null, p, chestHooks);
			} else {
				for (final ConfigCategory cc : p.getChildren())
					processEntry(p, cc, chestHooks);
			}
		}

		return chestHooks;
	}

	public static boolean areSchematicsInstalled() {
		return villageSchematics.size() != 0 || worldSchematics.size() != 0;
	}

	private static int villageStructureCount() {
		return villageSchematics.size();
	}

	private static int worldStructureCount() {
		return worldSchematics.size();
	}

	public static WeightTable<SchematicWeightItem> getTableForVillageGen() {
		final WeightTable<SchematicWeightItem> table = new WeightTable<SchematicWeightItem>();
		for (final SchematicWeightItem e : villageSchematics.getEntries()) {
			try {
				table.add((SchematicWeightItem) e.clone());
			} catch (final CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
		}

		return table;
	}

	public static WeightTable<SchematicWeightItem> getTableForWorldGen(int dimId, BiomeGenBase biome) {

		final WeightTable<SchematicWeightItem> table = new WeightTable<SchematicWeightItem>();
		for (final SchematicWeightItem e : worldSchematics.getEntries()) {
			final SchematicProperties p = e.properties;
			if (p.dimensions.isOk(dimId) && p.biomes.isOk(biome.biomeID))
				try {
					table.add((SchematicWeightItem) e.clone());
				} catch (final CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
		}

		return table;
	}

	public static List<ItemSeeds> getSeeds() {

		if (seeds != null)
			return seeds;

		seeds = new ArrayList<ItemSeeds>();
		for (final Item i : GameData.getItemRegistry().typeSafeIterable())
			if ((i instanceof ItemSeeds) && i != Items.pumpkin_seeds && i != Items.melon_seeds)
				seeds.add((ItemSeeds) i);

		return seeds;
	}

	protected static void dumpBiomeList() {

		ModLog.info("Detected biomes:");

		for (final BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
			if (b != null)
				ModLog.info("Biome [%s] id=%d", b.biomeName, b.biomeID);
	}

	public static void initialize() {

		ZipProcessor.initialize(accessPath, config, chests, schematicList);

		for (final SchematicProperties p : schematicList) {

			if (p.villageWeight > 0)
				villageSchematics.add(new SchematicWeightItem(p, true));

			if (p.worldWeight > 0)
				worldSchematics.add(new SchematicWeightItem(p, false));

			if (p.villageWeight > 0 || p.worldWeight > 0) {
				ModLog.info(p.toString());

				if (ModOptions.getEnableDebugLogging()) {
					Map<Block, Integer> analysis = p.analyze();
					for (final Entry<Block, Integer> e : analysis.entrySet()) {
						ModLog.info("Block: [%s], count=%d", e.getKey().getLocalizedName(), e.getValue().intValue());
					}
				}
			}
		}

		// Make initial calls to get the lists filled
		getChestGenerationHooks();
		getSeeds();

		// Save the configs so the player can edit
		config.save();
		chests.save();

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

		// Release the config files because they are
		// not needed from this point on - everything
		// is cached.
		config = null;
		chests = null;
	}
}
