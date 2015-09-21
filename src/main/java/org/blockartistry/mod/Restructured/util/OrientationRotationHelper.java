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

/*
 * This file was copied from Minecraft Forge and modified to suite the needs of
 * this mod.
 */

package org.blockartistry.mod.Restructured.util;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public final class OrientationRotationHelper {
	
	private OrientationRotationHelper() {}

	private static final Map<BlockType, BiMap<Integer, ForgeDirection>> MAPPINGS = new HashMap<BlockType, BiMap<Integer, ForgeDirection>>();

	/**
	 * Some blocks have the same rotation. The first of these blocks (sorted by
	 * itemID) should be listed as a type. Some of the types aren't actual
	 * blocks (helper types).
	 */
	private static enum BlockType {
		UNKNOWN,
		LOG,
		DISPENSER,
		BED,
		RAIL,
		RAIL_POWERED,
		RAIL_ASCENDING,
		RAIL_CORNER,
		TORCH,
		STAIR,
		CHEST,
		SIGNPOST,
		DOOR,
		LEVER,
		BUTTON,
		REDSTONE_REPEATER,
		TRAPDOOR,
		MUSHROOM_CAP,
		MUSHROOM_CAP_CORNER,
		MUSHROOM_CAP_SIDE,
		VINE,
		SKULL,
		ANVIL;

		private static final HashMap<Class<? extends Block>, BlockType> blockToType = new HashMap<Class<? extends Block>, BlockType>();

		static {
			blockToType.put(BlockLog.class, BlockType.LOG);
			blockToType.put(BlockOldLog.class,BlockType.LOG);
			blockToType.put(BlockNewLog.class, BlockType.LOG);
			
			blockToType.put(BlockTorch.class, BlockType.TORCH);
			blockToType.put(BlockStairs.class, BlockType.STAIR);

			blockToType.put(BlockBed.class, BlockType.BED);
			blockToType.put(BlockPumpkin.class, BlockType.BED);
			blockToType.put(BlockFenceGate.class, BlockType.BED);
			blockToType.put(BlockEndPortalFrame.class, BlockType.BED);
			blockToType.put(BlockTripWireHook.class, BlockType.BED);
			blockToType.put(BlockCocoa.class, BlockType.BED);

			blockToType.put(BlockRail.class, BlockType.RAIL);

			blockToType.put(BlockRailPowered.class, BlockType.RAIL_POWERED);
			blockToType.put(BlockRailDetector.class, BlockType.RAIL_POWERED);

			blockToType.put(BlockChest.class, BlockType.CHEST);
			blockToType.put(BlockEnderChest.class, BlockType.CHEST);
			blockToType.put(BlockFurnace.class, BlockType.CHEST);
			blockToType.put(BlockLadder.class, BlockType.CHEST);

			blockToType.put(BlockDoor.class, BlockType.DOOR);

			blockToType.put(BlockButton.class, BlockType.BUTTON);

			blockToType.put(BlockRedstoneRepeater.class,
					BlockType.REDSTONE_REPEATER);
			blockToType.put(BlockRedstoneComparator.class,
					BlockType.REDSTONE_REPEATER);

			blockToType.put(BlockTrapDoor.class, BlockType.TRAPDOOR);

			blockToType.put(BlockHugeMushroom.class, BlockType.MUSHROOM_CAP);

			blockToType.put(BlockVine.class, BlockType.VINE);

			blockToType.put(BlockSkull.class, BlockType.SKULL);

			blockToType.put(BlockAnvil.class, BlockType.ANVIL);

			blockToType.put(BlockDispenser.class, BlockType.DISPENSER);
			blockToType.put(BlockPistonBase.class, BlockType.DISPENSER);
			blockToType.put(BlockPistonExtension.class, BlockType.DISPENSER);
			blockToType.put(BlockHopper.class, BlockType.DISPENSER);

			blockToType.put(BlockLever.class, BlockType.LEVER);
		}

		/**
		 * This method looks for known UNKNOWN blocks.  Goal isn't to
		 * have *every* UNKNOWN, but to have the most common ones in order
		 * to shave off some compute cycles.
		 * 
		 * @param block
		 * @return
		 */
		private static boolean isKnownUnknown(final Block block) {
			return block.getClass() == Block.class || block == Blocks.air;
		}

		/**
		 * Based on the block instance type the method figures out the
		 * appropriate BlockType.
		 * 
		 * @param block The block to analyze
		 * @return BlockType associated with the block instance type
		 */
		private static BlockType myType(final Block block) {

			// Eliminate the common UNKNOWNS
			if (isKnownUnknown(block))
				return UNKNOWN;

			// Handle signs...
			if (block == Blocks.wall_sign)
				return CHEST;

			if (block == Blocks.standing_sign)
				return SIGNPOST;
			
			// The rest are (or will be) in the map
			BlockType bt = blockToType.get(block.getClass());
			if(bt == null) {
				// Can't find a direct lookup in the cache.  Do a slow trawl
				// looking for superclass matches.
				Class<? extends Block> searchFor = block.getClass();
				for (Entry<Class<? extends Block>, BlockType> e : blockToType
						.entrySet())
					if (e.getKey().isAssignableFrom(searchFor)) {
						bt = e.getValue();
						break;
					}
				
				// If we didn't find a superclass match then it's
				// an UNKNOWN.
				if(bt == null)
					bt = UNKNOWN;
				
				// At this point update the cache for the next block to
				// come in so we don't have to do the trawl.
				blockToType.put(searchFor, bt);
				
			}

			return bt;
		}
	}

	public static ForgeDirection getOrientation(final SelectedBlock block) {

		BlockType type = BlockType.myType(block.getBlock());
		if (type == BlockType.UNKNOWN)
			return ForgeDirection.UNKNOWN;

		return metadataToDirection(type, block.getMeta());
	}

	private static ForgeDirection metadataToDirection(final BlockType blockType,
			int meta) {
		if (blockType == BlockType.LEVER) {
			if (meta == 0x6) {
				meta = 0x5;
			} else if (meta == 0x0) {
				meta = 0x7;
			}
		}

		if (blockType == BlockType.BED)
			meta = meta & 3;

		if (MAPPINGS.containsKey(blockType)) {
			BiMap<Integer, ForgeDirection> biMap = MAPPINGS.get(blockType);
			if (biMap.containsKey(meta)) {
				return biMap.get(meta);
			}
		}

		if (blockType == BlockType.TORCH) {
			return ForgeDirection.getOrientation(6 - meta);
		}
		if (blockType == BlockType.STAIR) {
			return ForgeDirection.getOrientation(5 - (meta & 3));
		}
		if (blockType == BlockType.CHEST || blockType == BlockType.DISPENSER
				|| blockType == BlockType.SKULL || blockType == BlockType.BED) {
			return ForgeDirection.getOrientation(meta);
		}
		if (blockType == BlockType.BUTTON) {
			return ForgeDirection.getOrientation(6 - meta);
		}
		if (blockType == BlockType.TRAPDOOR) {
			return ForgeDirection.getOrientation((meta & 3) + 2).getOpposite();
		}

		if (blockType == BlockType.SIGNPOST) {
			if (meta < 3)
				return SOUTH;
			if (meta < 7)
				return WEST;
			if (meta < 11)
				return NORTH;
			if (meta < 16)
				return EAST;
			return SOUTH;

		}
		
		if(blockType == BlockType.LOG) {
			if(meta < 4)
				return UP;
			if(meta < 8)
				return EAST;
			if(meta < 0xC)
				return SOUTH;
		}

		return ForgeDirection.UNKNOWN;
	}

	static {
		BiMap<Integer, ForgeDirection> biMap;
/*
		biMap = HashBiMap.create(12);
		biMap.put(0x0, UP);
		biMap.put(0x1, UP);
		biMap.put(0x2, UP);
		biMap.put(0x3, UP);
		biMap.put(0x4, EAST);
		biMap.put(0x5, EAST);
		biMap.put(0x6, EAST);
		biMap.put(0x7, EAST);
		biMap.put(0x8, SOUTH);
		biMap.put(0x9, SOUTH);
		biMap.put(0xA, SOUTH);
		biMap.put(0xB, SOUTH);
		MAPPINGS.put(BlockType.LOG, biMap);
*/
		biMap = HashBiMap.create(4);
		biMap.put(0x0, SOUTH);
		biMap.put(0x1, WEST);
		biMap.put(0x2, NORTH);
		biMap.put(0x3, EAST);
		MAPPINGS.put(BlockType.BED, biMap);

		biMap = HashBiMap.create(4);
		biMap.put(0x2, EAST);
		biMap.put(0x3, WEST);
		biMap.put(0x4, NORTH);
		biMap.put(0x5, SOUTH);
		MAPPINGS.put(BlockType.RAIL_ASCENDING, biMap);

		biMap = HashBiMap.create(4);
		biMap.put(0x6, WEST);
		biMap.put(0x7, NORTH);
		biMap.put(0x8, EAST);
		biMap.put(0x9, SOUTH);
		MAPPINGS.put(BlockType.RAIL_CORNER, biMap);

		biMap = HashBiMap.create(6);
		biMap.put(0x1, EAST);
		biMap.put(0x2, WEST);
		biMap.put(0x3, SOUTH);
		biMap.put(0x4, NORTH);
		biMap.put(0x5, UP);
		biMap.put(0x7, DOWN);
		MAPPINGS.put(BlockType.LEVER, biMap);

		biMap = HashBiMap.create(4);
		biMap.put(0x0, WEST);
		biMap.put(0x1, NORTH);
		biMap.put(0x2, EAST);
		biMap.put(0x3, SOUTH);
		MAPPINGS.put(BlockType.DOOR, biMap);

		biMap = HashBiMap.create(4);
		biMap.put(0x0, NORTH);
		biMap.put(0x1, EAST);
		biMap.put(0x2, SOUTH);
		biMap.put(0x3, WEST);
		MAPPINGS.put(BlockType.REDSTONE_REPEATER, biMap);

		biMap = HashBiMap.create(4);
		biMap.put(0x1, EAST);
		biMap.put(0x3, SOUTH);
		biMap.put(0x7, NORTH);
		biMap.put(0x9, WEST);
		MAPPINGS.put(BlockType.MUSHROOM_CAP_CORNER, biMap);

		biMap = HashBiMap.create(4);
		biMap.put(0x2, NORTH);
		biMap.put(0x4, WEST);
		biMap.put(0x6, EAST);
		biMap.put(0x8, SOUTH);
		MAPPINGS.put(BlockType.MUSHROOM_CAP_SIDE, biMap);

		biMap = HashBiMap.create(2);
		biMap.put(0x0, SOUTH);
		biMap.put(0x1, EAST);
		MAPPINGS.put(BlockType.ANVIL, biMap);

		biMap = HashBiMap.create(4);
		biMap.put(0x1, SOUTH);
		biMap.put(0x2, WEST);
		biMap.put(0x4, NORTH);
		biMap.put(0x8, EAST);
		MAPPINGS.put(BlockType.VINE, biMap);
	}
}