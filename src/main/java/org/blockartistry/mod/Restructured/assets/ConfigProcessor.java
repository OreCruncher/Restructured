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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.Restructured.schematica.SchematicFormat;
import org.blockartistry.mod.Restructured.util.ElementRule;
import org.blockartistry.mod.Restructured.util.MyUtils;

import com.google.common.base.Predicate;

import org.blockartistry.mod.Restructured.util.ElementRule.Rule;
import org.blockartistry.mod.Restructured.util.JarConfiguration;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ConfigProcessor {

	private ConfigProcessor() {
	}

	public static class ChestsConfigFilter implements Predicate<ZipEntry> {
		@Override
		public boolean apply(final ZipEntry t) {
			return t.getName().contains("chests.cfg");
		}
	}

	public static class SchematicsConfigFilter implements Predicate<ZipEntry> {
		@Override
		public boolean apply(final ZipEntry t) {
			return t.getName().contains("schematics.cfg");
		}
	}

	public static class SchematicFilter implements Predicate<ZipEntry> {
		@Override
		public boolean apply(final ZipEntry t) {
			return t.getName().endsWith(Assets.SCHEMATIC_RESOURCE_EXTENSION);
		}
	}

	/**
	 * Processes a chests.cfg file from a Zip file.
	 */
	public static class ChestsConfigProcess implements Predicate<Object[]> {

		private final Configuration target;

		public ChestsConfigProcess(final Configuration config) {
			this.target = config;
		}

		@Override
		public boolean apply(final Object[] input) {
			final String prefix = (String) input[0];
			// final ZipEntry entry = (ZipEntry)input[1];
			final InputStream stream = (InputStream) input[2];

			// The input stream contains the config file we need
			// to merge into the master.
			final JarConfiguration src = new JarConfiguration(stream);
			final ConfigCategory c = src.getCategory(Assets.CONFIG_CHESTS);

			// If the property in the chests.cfg has not been
			// initialized copy it from the ZIP.
			for (final ConfigCategory p : c.getChildren()) {
				final String name = Assets.CONFIG_CHESTS + "." + prefix + "." + p.getName();
				ConfigCategory temp = target.getCategory(name);
				if (temp.isEmpty()) {
					for (Entry<String, Property> item : p.getValues().entrySet()) {
						temp.put(item.getKey(), item.getValue());
					}
				}
			}

			return true;
		}
	}

	/**
	 * Processes a schematics.cfg file from a Zip file.
	 */
	public static class SchematicsConfigProcess implements Predicate<Object[]> {

		private final Configuration target;

		public SchematicsConfigProcess(final Configuration config) {
			this.target = config;
		}

		@Override
		public boolean apply(final Object[] input) {
			final String prefix = (String) input[0];
			// final ZipEntry entry = (ZipEntry)input[1];
			final InputStream stream = (InputStream) input[2];

			// The input stream contains the config file we need
			// to merge into the master.
			final JarConfiguration src = new JarConfiguration(stream);
			final ConfigCategory c = src.getCategory(Assets.CONFIG_STRUCTURES);

			// If the property in the chests.cfg has not been
			// initialized copy it from the ZIP.
			for (final ConfigCategory p : c.getChildren()) {
				final String name = Assets.CONFIG_STRUCTURES + "." + prefix + "." + p.getName();
				ConfigCategory temp = target.getCategory(name);
				if (temp.isEmpty()) {
					for (Entry<String, Property> item : p.getValues().entrySet()) {
						if (item.getKey().equals(Assets.OPTION_CHEST_CONTENTS)
								&& !item.getValue().getString().isEmpty()) {
							final Property prop = new Property(Assets.OPTION_CHEST_CONTENTS,
									prefix + "." + item.getValue().getString(), Property.Type.STRING);
							temp.put(item.getKey(), prop);
						} else
							temp.put(item.getKey(), item.getValue());

					}
				}
			}

			return true;
		}
	}

	/**
	 * Processes a .schematic file from a Zip file.
	 */
	public static class SchematicsProcess implements Predicate<Object[]> {

		private final Configuration target;
		private final List<SchematicProperties> schematicList;

		public SchematicsProcess(final Configuration config, final List<SchematicProperties> list) {
			this.target = config;
			this.schematicList = list;
		}

		@Override
		public boolean apply(final Object[] input) {

			final String prefix = (String) input[0];
			final ZipEntry entry = (ZipEntry) input[1];
			final InputStream stream = (InputStream) input[2];

			final SchematicProperties props = new SchematicProperties();
			props.name = prefix + "." + StringUtils.removeEnd(entry.getName(), Assets.SCHEMATIC_RESOURCE_EXTENSION);

			final String category = Assets.CONFIG_STRUCTURES + "." + props.name;

			props.villageWeight = target.getInt(Assets.OPTION_VILLAGE_WEIGHT, category, Assets.DEFAULT_VILLAGE_WEIGHT,
					0, Integer.MAX_VALUE, "Relative selection weight for village structure generation");

			props.worldWeight = target.getInt(Assets.OPTION_WORLD_WEIGHT, category, Assets.DEFAULT_WORLD_WEIGHT, 0,
					Integer.MAX_VALUE, "Relative selection weight for world generation");

			props.villagerCount = target.getInt(Assets.OPTION_VILLAGER_COUNT, category, Assets.DEFAULT_VILLAGER_COUNT,
					-1, Integer.MAX_VALUE, "Number of villagers to spawn for the structure (-1 random)");

			props.villagerProfession = target.getInt(Assets.OPTION_VILLAGER_PROFESSION, category,
					Assets.DEFAULT_VILLAGER_PROFESSION, -1, 4,
					"Villager profession: -1 random, 0 farmer, 1 librarian, 2 priest, 3 smith, 4 butcher");

			props.limit = target.getInt(Assets.OPTION_LIMIT, category, Assets.DEFAULT_LIMIT, 0, Integer.MAX_VALUE,
					"Maximum number of this type of structure to have in a village");

			props.groundOffset = target.getInt(Assets.OPTION_OFFSET, category, Assets.DEFAULT_OFFSET, 0,
					Integer.MAX_VALUE, "The number of blocks below ground the structure extends");

			props.chestContents = target.getString(Assets.OPTION_CHEST_CONTENTS, category,
					Assets.DEFAULT_CHEST_CONTENTS, "What chest generation hook to use when filling chests");

			props.chestContentsCount = target.getInt(Assets.OPTION_CHEST_CONTENTS_COUNT, category,
					Assets.DEFAULT_CHEST_CONTENTS_COUNT, 0, Integer.MAX_VALUE,
					"The number of stacks to pull from the generation table");

			props.spawnerEnableChance = target.getInt(Assets.OPTION_SPAWNER_ENABLE_CHANCE, category,
					Assets.DEFAULT_SPAWNER_ENABLE_CHANCE, 0, 100,
					"Chance that a spawner will be preserved when placed");

			boolean asBlackList = target.getBoolean(Assets.OPTION_BIOME_LIST_TYPE, category,
					Assets.DEFAULT_BIOME_LIST_TYPE, "Treat the biome list as a blacklist vs. whitelist");
			String def = MyUtils.join(";", Assets.DEFAULT_BIOME_LIST);
			String list = target.getString(Assets.OPTION_BIOME_LIST, category, def, "List of biome IDs");

			try {
				props.biomes = new ElementRule(asBlackList ? Rule.MUST_NOT_BE_IN : Rule.MUST_BE_IN,
						MyUtils.split(";", list));
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			asBlackList = target.getBoolean(Assets.OPTION_DIMENSION_LIST_TYPE, category,
					Assets.DEFAULT_DIMENSION_LIST_TYPE, "Treat the dimension list as a blacklist vs. whitelist");
			def = MyUtils.join(";", Assets.DEFAULT_DIMENSION_LIST);
			list = target.getString(Assets.OPTION_DIMENSION_LIST, category, def, "List of dimension IDs");

			String options = target.getString(Assets.OPTION_OPTIONS, category, Assets.DEFAULT_OPTIONS,
					"Options for generation");
			props.suppressFire = options.contains(Assets.OPTION_OPTIONS_SUPPRESS_FIRE);
			props.suppressMonsterEgg = options.contains(Assets.OPTION_OPTIONS_SUPPRESS_EGGS);
			props.randomizeCrops = options.contains(Assets.OPTION_OPTIONS_RANDOM_CROPS);

			try {
				props.dimensions = new ElementRule(asBlackList ? Rule.MUST_NOT_BE_IN : Rule.MUST_BE_IN,
						MyUtils.split(";", list));
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				props.schematic = SchematicFormat.readFromStream(stream);
			} catch (final Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					stream.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}

			if (props.schematic != null)
				schematicList.add(props);

			return true;
		}
	}
}
