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

package org.blockartistry.mod.Restructured.world;

import java.lang.reflect.Field;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.ModOptions;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;

public class TerrainEventBusHandler {

	//private static Field terrainType;
	private static Field density;
	private static Field minDistance;
	
	static {
		
		try {
			
			//terrainType = ReflectionHelper.findField(MapGenVillage.class, "terrainType", "field_75054_f");
			density = ReflectionHelper.findField(MapGenVillage.class, "field_82665_g");
			minDistance = ReflectionHelper.findField(MapGenVillage.class, "field_82666_h");
			
		} catch(final Throwable t) {
			ModLog.warn("Unable to hook MapGenVillage parameters");
			//terrainType = null;
			density = null;
			minDistance = null;
		}
	}
	
	private TerrainEventBusHandler() {
	}
	
	public static void initialize() {
		MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventBusHandler());
	}

	@SubscribeEvent
	public void onInitMapGenEvent(final InitMapGenEvent event) {
		
		// Hook village generation
		if(event.type == EventType.VILLAGE) {
			
			if(event.newGen instanceof MapGenVillage) {
				
				try {
					int t = ModOptions.getVillageDensity();
					if(t > 0)
						density.setInt(event.newGen, t);
					
					t = ModOptions.getMinimumVillageDistance();
					if(t > 0)
						minDistance.setInt(event.newGen, t);
					
				} catch(final Throwable t) {
					ModLog.error("Unable to set village generation parameters", t);
				}
			}
		}
	}
}
