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

package org.blockartistry.mod.Restructured.world.village.themes;

import org.blockartistry.mod.Restructured.util.SelectedBlock;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;

/*
 * Hooks the Forge events to replace blocks during village/world
 * generation.
 */
public class VillageBlockReplacement {
	
	private VillageBlockReplacement() {
	}

	public static void initialize() {
		DesertVillageTheme.initialize();
		BirchForestVillageTheme.initialize();
		ForestVillageTheme.initialize();
		TaigaForestVillageTheme.initialize();
		JungleVillageTheme.initialize();
		RoofedForestVillageTheme.initialize();
		SavannaVillageTheme.initialize();
		BeachVillageTheme.initialize();

		MinecraftForge.TERRAIN_GEN_BUS.register(new VillageBlockReplacement());
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void villageBlockIdEvent(final BiomeEvent.GetVillageBlockID event) {
		if(event.getResult() == Result.DENY)
			return;
		final VillageTheme theme = VillageTheme.find(event.biome);
		final Block replace = theme.findReplacementBlock(new SelectedBlock(event.original, event.type), true);
		if(replace != event.original) {
			event.replacement = replace;
			event.setResult(Result.DENY);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void villageBlockMetaEvent(final BiomeEvent.GetVillageBlockMeta event) {
		if(event.getResult() == Result.DENY)
			return;
		final VillageTheme theme = VillageTheme.find(event.biome);
		final int replace = theme.findReplacementMeta(new SelectedBlock(event.original, event.type), true);
		if(replace != event.type) {
			event.replacement = replace;
			event.setResult(Result.DENY);
		}
	}
}
