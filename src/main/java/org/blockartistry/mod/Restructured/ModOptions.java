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

package org.blockartistry.mod.Restructured;

import org.blockartistry.mod.Restructured.util.ElementRule;
import org.blockartistry.mod.Restructured.util.ElementRule.Rule;
import org.blockartistry.mod.Restructured.util.MyUtils;

import net.minecraftforge.common.config.Configuration;

public final class ModOptions {

	private ModOptions() {
	}

	protected static final String CATEGORY_GLOBAL = "global";

	protected static final String CATEGORY_LOGGING_CONTROL = "logging";
	protected static final String CONFIG_ENABLE_DEBUG_LOGGING = "Enable Debug Logging";
	protected static boolean enableDebugLogging = false;
	protected static final String CONFIG_ENABLE_ONLINE_VERSION_CHECK = "Enable Online Version Check";
	protected static boolean enableOnlineVersionCheck = true;

	protected static final String CATEGORY_GENERATION = "generation";
	protected static final String CONFIG_ADDITIONAL_VILLAGE_BIOMES = "additionalVillageBiomes";
	protected static int[] additionalVillageBiomes = new int[] {};
	protected static final String CONFIG_MIN_VILLAGE_DISTANCE = "Minimum distance between villages";
	protected static int minimumVillageDistance = 0;
	protected static final String CONFIG_VILLAGE_DENSITY = "Village Density";
	protected static int villageDensity = 0;
	protected static final String CONFIG_GENERATION_CHANCE = "Generation Chance";
	protected static int generationChance = 65;
	protected static final String CONFIG_ENABLE_THEMING = "Enable Theming";
	protected static boolean enableTheming = true;
	protected static final String CONFIG_DIMENSION_LIST = "Dimension List";
	protected static String dimensionList = "0";
	protected static final String CONFIG_DIMENSION_LIST_TYPE = "Dimension List as Blacklist";
	protected static boolean dimensionListAsBlacklist = false;

	protected static final String CATEGORY_MOB_CONTROL = "mobcontrol";
	protected static final String CONFIG_BLOCK_CREEPER_EXPLOSION = "Block Creeper Explosion";
	protected static boolean blockCreeperExplosion = false;
	protected static final String CONFIG_BLOCK_ENDERMAN_GRIEFING = "Block Enderman Griefing";
	protected static boolean blockEndermanGriefing = false;
	protected static final String CONFIG_BLOCK_TREE_SPAWN = "Block spawning in/on trees";
	protected static boolean blockMobsSpawningInTrees = true;

	protected static final String CATEGORY_MOB_SPAWN_FACTORS = "mobcontrol.spawn";
	protected static final String CONFIG_MOB_SPAWN_MOB_FACTOR = "Monsters";
	protected static int mobSpawnMobFactor = 0;
	protected static final String CONFIG_MOB_SPAWN_ANIMAL_FACTOR = "Animals";
	protected static int mobSpawnAnimalFactor = 0;
	protected static final String CONFIG_MOB_SPAWN_AMBIENT_FACTOR = "Ambient";
	protected static int mobSpawnAmbientFactor = 0;
	protected static final String CONFIG_MOB_SPAWN_WATER_FACTOR = "Water Critters";
	protected static int mobSpawnWaterFactor = 0;

	public static void load(final Configuration config) {

		// CATEGORY_GLOBAL

		// CATEGORY_LOGGING_CONTROL
		String comment = "Enables/disables online version checking";
		enableOnlineVersionCheck = config.getBoolean(CONFIG_ENABLE_ONLINE_VERSION_CHECK, CATEGORY_LOGGING_CONTROL,
				enableOnlineVersionCheck, comment);

		comment = "Enables/disables debug logging of the mod";
		enableDebugLogging = config.getBoolean(CONFIG_ENABLE_DEBUG_LOGGING, CATEGORY_LOGGING_CONTROL,
				enableDebugLogging, comment);

		// CATEGORY_GENERATION
		final String list = config.getString(CONFIG_ADDITIONAL_VILLAGE_BIOMES, CATEGORY_GENERATION, "",
				"Additional biomes to enable village generation");
		try {
			additionalVillageBiomes = MyUtils.splitToInts(list, ';');
		} catch (Exception e) {
			ModLog.warn("Bad biome information for additional biome village generation");
		}

		comment = "Distance between villages (0 = Minecraft default)";
		minimumVillageDistance = config.getInt(CONFIG_MIN_VILLAGE_DISTANCE, CATEGORY_GENERATION, minimumVillageDistance,
				0, Integer.MAX_VALUE, comment);

		comment = "Village Density (0 = Minecraft default)";
		villageDensity = config.getInt(CONFIG_VILLAGE_DENSITY, CATEGORY_GENERATION, villageDensity, 0,
				Integer.MAX_VALUE, comment);

		comment = "1-in-N chance of generating a world feature per chunk (lower more frequent; 0 disable)";
		generationChance = config.getInt(CONFIG_GENERATION_CHANCE, CATEGORY_GENERATION, generationChance, 0,
				Integer.MAX_VALUE, comment);

		comment = "Enables/disables biome theming of structures";
		enableTheming = config.getBoolean(CONFIG_ENABLE_THEMING, CATEGORY_GENERATION, enableTheming, comment);

		comment = "List of dimensions to black/white list";
		dimensionList = config.getString(CONFIG_DIMENSION_LIST, CATEGORY_GENERATION, dimensionList, comment);

		comment = "Treat Dimension List as a Blacklist vs. Whitelist";
		dimensionListAsBlacklist = config.getBoolean(CONFIG_DIMENSION_LIST_TYPE, CATEGORY_GENERATION,
				dimensionListAsBlacklist, comment);

		// CATEGORY_MOB_CONTROL
		comment = "Prevent block destruction due to Creeper explosions";
		blockCreeperExplosion = config.getBoolean(CONFIG_BLOCK_CREEPER_EXPLOSION, CATEGORY_MOB_CONTROL,
				blockCreeperExplosion, comment);

		comment = "Prevent Enderman from picking up blocks";
		blockEndermanGriefing = config.getBoolean(CONFIG_BLOCK_ENDERMAN_GRIEFING, CATEGORY_MOB_CONTROL,
				blockEndermanGriefing, comment);

		comment = "Prevent mobs spawning in or on trees";
		blockMobsSpawningInTrees = config.getBoolean(CONFIG_BLOCK_TREE_SPAWN, CATEGORY_MOB_CONTROL,
				blockMobsSpawningInTrees, comment);

		// CATEGORY_MOB_SPAWN_FACTORS
		comment = "Spawn factor for monsters (0 use Vanilla)";
		mobSpawnMobFactor = config.getInt(CONFIG_MOB_SPAWN_MOB_FACTOR, CATEGORY_MOB_SPAWN_FACTORS, mobSpawnMobFactor, 0,
				Integer.MAX_VALUE, comment);

		comment = "Spawn factor for animals (0 use Vanilla)";
		mobSpawnAnimalFactor = config.getInt(CONFIG_MOB_SPAWN_ANIMAL_FACTOR, CATEGORY_MOB_SPAWN_FACTORS,
				mobSpawnAnimalFactor, 0, Integer.MAX_VALUE, comment);

		comment = "Spawn factor for ambient creatures (0 use Vanilla)";
		mobSpawnAmbientFactor = config.getInt(CONFIG_MOB_SPAWN_AMBIENT_FACTOR, CATEGORY_MOB_SPAWN_FACTORS,
				mobSpawnAmbientFactor, 0, Integer.MAX_VALUE, comment);

		comment = "Spawn factor for water creatures - GO SQUIDS! (0 use Vanilla)";
		mobSpawnWaterFactor = config.getInt(CONFIG_MOB_SPAWN_WATER_FACTOR, CATEGORY_MOB_SPAWN_FACTORS,
				mobSpawnWaterFactor, 0, Integer.MAX_VALUE, comment);
	}

	public static boolean getOnlineVersionChecking() {
		return true;
	}

	public static boolean getEnableDebugLogging() {
		return enableDebugLogging;
	}

	public static int[] getAdditionalVillageBiomes() {
		return additionalVillageBiomes;
	}

	public static int getMinimumVillageDistance() {
		return minimumVillageDistance;
	}

	public static int getVillageDensity() {
		return villageDensity;
	}

	public static int getGenerationChance() {
		return generationChance;
	}

	public static boolean getEnableTheming() {
		return enableTheming;
	}

	public static boolean getBlockCreeperExplosion() {
		return blockCreeperExplosion;
	}

	public static boolean getBlockEndermanGriefing() {
		return blockEndermanGriefing;
	}

	public static boolean getBlockMobsSpawningInTrees() {
		return blockMobsSpawningInTrees;
	}

	public static int getMobSpawnMobFactor() {
		return mobSpawnMobFactor;
	}

	public static int getMobSpawnAnimalFactor() {
		return mobSpawnAnimalFactor;
	}

	public static int getMobSpawnAmbientFactor() {
		return mobSpawnAmbientFactor;
	}

	public static int getMobSpawnWaterFactor() {
		return mobSpawnWaterFactor;
	}

	public static ElementRule getGlobalDimensionRule() {
		return new ElementRule(dimensionListAsBlacklist ? Rule.MUST_NOT_BE_IN : Rule.MUST_BE_IN,
				MyUtils.splitToInts(dimensionList, ','));
	}
}
