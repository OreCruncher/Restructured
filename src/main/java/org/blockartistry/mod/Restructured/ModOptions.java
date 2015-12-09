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

	public static void load(final Configuration config) {

		// CATEGORY_GLOBAL

		// CATEGORY_LOGGING_CONTROL
		String comment = "Enables/disables online version checking";
		enableOnlineVersionCheck = config.getBoolean(CONFIG_ENABLE_ONLINE_VERSION_CHECK, CATEGORY_LOGGING_CONTROL,
				enableOnlineVersionCheck, comment );

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
}
