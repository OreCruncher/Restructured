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
import java.util.IdentityHashMap;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.ModOptions;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;

public final class MobControl {

	private MobControl() {
	}

	private static void setup(final EnumCreatureType type, final int factor) {
		if (factor != 0)
			type.maxNumberOfCreature = factor;
		ModLog.info("Mob %s spawn factor is %d", type.name(), type.maxNumberOfCreature);
	}

	public static void initialize() {
		setup(EnumCreatureType.ambient, ModOptions.getMobSpawnAmbientFactor());
		setup(EnumCreatureType.creature, ModOptions.getMobSpawnAnimalFactor());
		setup(EnumCreatureType.monster, ModOptions.getMobSpawnMobFactor());
		setup(EnumCreatureType.waterCreature, ModOptions.getMobSpawnWaterFactor());

		if (ModOptions.getBlockCreeperExplosion()) {
			ModLog.info("Blocking Creeper block damage");
			MinecraftForge.EVENT_BUS.register(new MobControl());
		}

		if (ModOptions.getBlockEndermanGriefing()) {
			try {
				final Field carriable = ReflectionHelper.findField(EntityEnderman.class, "carriable");
				if (carriable != null) {
					carriable.set(null, new IdentityHashMap<Block, Boolean>());
					ModLog.info("Blocking Enderman griefing");
				}
			} catch (final Exception ex) {
				ModLog.warn("Unable to access Enderman block table");
			}
		}
	}

	@SubscribeEvent
	public void onExplosion(final ExplosionEvent.Detonate event) {
		if (event.explosion.exploder instanceof EntityCreeper) {
			event.explosion.affectedBlockPositions = ImmutableList.of();
		}
	}
}
