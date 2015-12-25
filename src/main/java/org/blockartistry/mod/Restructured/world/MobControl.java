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

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.ExplosionEvent;

public final class MobControl {
	
	private static final boolean BLOCK_CREEPER_BLOCK_DAMAGE = ModOptions.getBlockCreeperExplosion();
	private static final boolean BLOCK_MOB_TREE_SPAWNING = ModOptions.getBlockMobsSpawningInTrees();

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

		if (BLOCK_CREEPER_BLOCK_DAMAGE || BLOCK_MOB_TREE_SPAWNING) {
			if(BLOCK_CREEPER_BLOCK_DAMAGE)
				ModLog.info("Blocking Creeper block damage");
			if(BLOCK_MOB_TREE_SPAWNING)
				ModLog.info("Blocking mob tree spawning");
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
		if(!BLOCK_CREEPER_BLOCK_DAMAGE)
			return;
		if (event.explosion.exploder instanceof EntityCreeper)
			event.explosion.affectedBlockPositions = ImmutableList.of();
	}

	@SubscribeEvent
	public void onMobSpawn(final LivingSpawnEvent.CheckSpawn event) {
		if(!BLOCK_MOB_TREE_SPAWNING)
			return;
		final int x = MathHelper.floor_float(event.x);
		final int y = MathHelper.floor_float(event.y) - 1;
		final int z = MathHelper.floor_float(event.z);
		final Block block = event.world.getBlock(x, y, z); 
		if (block.canSustainLeaves(event.world, x, y, z))
			event.setResult(Event.Result.DENY);
	}
}
