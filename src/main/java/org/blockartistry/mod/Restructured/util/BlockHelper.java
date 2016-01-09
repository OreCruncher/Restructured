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

package org.blockartistry.mod.Restructured.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.IPlantable;

public class BlockHelper {

	private static final PropertyDirection FACING = PropertyDirection.create("facing");

	private BlockHelper() { }
	
	public static boolean isAir(final IBlockState state) {
		return isAir(state.getBlock());
	}
	
	public static boolean isAir(final Block block) {
		return block == Blocks.air;
	}

	public static boolean canBreath(final IBlockState state) {
		return canBreath(state.getBlock());
	}
	
	public static boolean canBreath(final Block block) {
		return isAir(block) || !block.getMaterial().isSolid();
	}

	public static boolean isChest(final IBlockState state) {
		return isChest(state.getBlock());
	}
	
	public static boolean isChest(final Block block) {
		return block == Blocks.chest || block == Blocks.ender_chest || block == Blocks.trapped_chest;
	}

	public static boolean isAnvil(final IBlockState state) {
		return isAnvil(state.getBlock());
	}
	
	public static boolean isAnvil(final Block block) {
		return block == Blocks.anvil;
	}
	
	public static boolean isLadder(final IBlockState state) {
		return isLadder(state.getBlock());
	}
	
	public static boolean isLadder(final Block block) {
		return block == Blocks.ladder;
	}
	
	public static boolean isFurnace(final IBlockState state) {
		return isFurnace(state.getBlock());
	}
	
	public static boolean isFurnace(final Block block) {
		return block == Blocks.furnace || block == Blocks.lit_furnace;
	}
	
	public static boolean isLava(final IBlockState state) {
		return isLava(state.getBlock());
	}
	
	public static boolean isLava(final Block block) {
		return block == Blocks.lava || block == Blocks.flowing_lava;
	}

	public static boolean isWater(final IBlockState state) {
		return isWater(state.getBlock());
	}
	
	public static boolean isWater(final Block block) {
		return block == Blocks.water || block == Blocks.flowing_water;
	}

	public static boolean isFire(final IBlockState state) {
		return isFire(state.getBlock());
	}
	
	public static boolean isFire(final Block block) {
		return block == Blocks.fire;
	}

	public static boolean isFireSource(final IBlockState state) {
		return isFireSource(state.getBlock());
	}
	
	public static boolean isFireSource(final Block block) {
		return isFire(block) || isLava(block);
	}
	
	public static boolean isTorch(final IBlockState state) {
		return isTorch(state.getBlock());
	}
	
	public static boolean isTorch(final Block block) {
		return block == Blocks.torch || block == Blocks.redstone_torch;
	}
	
	public static boolean isLever(final IBlockState state) {
		return isLever(state.getBlock());
	}
	
	public static boolean isLever(final Block block) {
		return block == Blocks.lever;
	}
	
	public static boolean isButton(final IBlockState state) {
		return isButton(state.getBlock());
	}
	
	public static boolean isButton(final Block block) {
		return block == Blocks.stone_button || block == Blocks.wooden_button;
	}
	
	public static boolean isDoor(final IBlockState state) {
		return isDoor(state.getBlock());
	}
	
	public static boolean isDoor(final Block block) {
		return block instanceof BlockDoor || block instanceof BlockTrapDoor;
	}

	public static boolean isSpawner(final IBlockState state) {
		return isSpawner(state.getBlock());
	}
	
	public static boolean isSpawner(final Block block) {
		return block == Blocks.mob_spawner;
	}

	public static boolean isMonsterEgg(final IBlockState state) {
		return isMonsterEgg(state.getBlock());
	}
	
	public static boolean isMonsterEgg(final Block block) {
		return block == Blocks.monster_egg;
	}
	
	public static boolean isLiquid(final IBlockState state) {
		return isLiquid(state.getBlock());
	}
	
	public static boolean isLiquid(final Block block) {
		return block.getMaterial().isLiquid();
	}
	
	public static boolean isSolid(final IBlockState state) {
		return isSolid(state.getBlock());
	}
	
	public static boolean isSolid(final Block block) {
		return block.getMaterial().isSolid();
	}
	
	public static boolean isPlantable(final IBlockState state) {
		return isPlantable(state.getBlock());
	}
	
	public static boolean isPlantable(final Block block) {
		return block instanceof IPlantable;
	}
	
	public static boolean isGrowable(final IBlockState state) {
		return isGrowable(state.getBlock());
	}
	
	public static boolean isGrowable(final Block block) {
		return block instanceof IGrowable;
	}
	
	public static boolean isCrop(final IBlockState state) {
		return isCrop(state.getBlock());
	}
	
	public static boolean isCrop(final Block block) {
		return block instanceof BlockCrops;
	}
	
	public static boolean isSlab(final IBlockState state) {
		return isSlab(state.getBlock());
	}
	
	public static boolean isSlab(final Block block) {
		return block == Blocks.wooden_slab || block == Blocks.stone_slab;
	}
	
	public static boolean isLog(final IBlockState state) {
		return isLog(state.getBlock());
	}
	
	public static boolean isLog(final Block block) {
		return block == Blocks.log || block == Blocks.log2;
	}
	
	public static boolean isPlank(final IBlockState state) {
		return isPlank(state.getBlock());
	}
	
	public static boolean isPlank(final Block block) {
		return block == Blocks.planks;
	}
	
	public static EnumFacing getOrientation(final IBlockState state) {
		if (state.getPropertyNames().contains(FACING))
			return state.getValue(FACING);
		return null;
	}

	public static IBlockState rotate(final IBlockState state, final int count) {

		if (count == 0)
			return state;

		// Use our Fantasy to satisfy Minecraft so we can rotate
		// the block without having to place it into the world.
		// Should expose methods that don't rely on a world instance. :\
		EnumFacing current = getOrientation(state);
		if (current == null || current == EnumFacing.UP || current == EnumFacing.DOWN)
			return state;

		for (int i = 0; i < count; i++)
			current = current.rotateY();

		return state.withProperty(FACING, current);
	}
}
