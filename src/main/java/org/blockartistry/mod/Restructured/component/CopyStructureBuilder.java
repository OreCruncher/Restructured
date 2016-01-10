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

package org.blockartistry.mod.Restructured.component;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.ChestGenHooks;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.schematica.Schematic;
import org.blockartistry.mod.Restructured.schematica.Schematic.SchematicEntity;
import org.blockartistry.mod.Restructured.schematica.Schematic.SchematicTileEntity;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Dimensions;

public class CopyStructureBuilder {

	protected static final Random rand = new Random();

	protected final IStructureBuilder structure;
	protected final World world;
	protected final StructureBoundingBox box;
	protected final EnumFacing orientation;
	protected final SchematicProperties properties;

	protected final Set<BlockPos> waitToPlace = new HashSet<BlockPos>();
	protected final Set<BlockPos> blockList = new HashSet<BlockPos>();

	public CopyStructureBuilder(final World world, final StructureBoundingBox box, final EnumFacing direction,
			final SchematicProperties properties, final IStructureBuilder structure) {

		this.world = world;
		this.box = box;
		this.orientation = direction;
		this.structure = structure;
		this.properties = properties;
	}

	public void place(final IBlockState state, final BlockPos pos) {
		this.structure.placeBlock(this.world, handleRotation(state), pos, this.box);
	}

	public boolean isVecInside(final BlockPos pos, final StructureBoundingBox box) {
		return this.structure.isVecInside(pos, box);
	}

	public void generate() {

		final Schematic schematic = this.properties.schematic;
		final Dimensions size = this.structure.getDimensions();

		// Scan from the ground up. This is important when
		// replacing crops.
		for (int y = 0; y < size.height; y++)
			for (int x = 0; x < size.width; x++)
				for (int z = 0; z < size.length; z++) {

					final BlockPos v = new BlockPos(x, y, z);
					if (isVecInside(v, this.box)) {

						final IBlockState state = schematic.getBlockState(v);

						// Do we skip placement?
						if (doSkipSpawnerPlacement(state, v))
							continue;

						// Delay placing things that don't like being
						// rotated or attached to blocks that change
						if (waitToPlace(state)) {
							this.waitToPlace.add(v);
							continue;
						}

						place(state, v);
					}
				}

		for (final BlockPos v : this.waitToPlace) {
			final IBlockState state = schematic.getBlockState(v);
			place(state, v);
		}

		for (final SchematicTileEntity e : schematic.getTileEntities()) {
			final BlockPos coords = e.coords;
			if (!isVecInside(coords, this.box))
				continue;

			// If the block location is black listed we don't want
			// to create the tile entity at the location
			if (this.blockList.contains(coords))
				continue;

			try {
				final TileEntity entity = (TileEntity) e.getInstance(world);
				entity.validate();

				// Place it into the world
				final BlockPos worldCoord = this.structure.getWorldCoordinates(coords);
				this.world.removeTileEntity(worldCoord);
				this.world.setTileEntity(worldCoord, entity);

				final IBlockState state = schematic.getBlockState(coords);
				if (doFillChestContents(state)) {
					generateChestContents((IInventory) entity, this.properties.chestContents,
							this.properties.chestContentsCount);
				}

			} catch (Exception ex) {
				ModLog.warn("Unable to place TileEntity");
				ex.printStackTrace();
			}
		}

		for (final SchematicEntity e : schematic.getEntities()) {
			if (!isVecInside(e.coords, this.box))
				continue;

			try {
				final Entity entity = (Entity) e.getInstance(this.world);
				final BlockPos coord = this.structure.getWorldCoordinates(e.coords);
				entity.setPosition(coord.getX(), coord.getY(), coord.getZ());

				if (entity instanceof EntityHanging) {
					final EntityHanging howsIt = (EntityHanging) entity;
					howsIt.updateFacingWithBoundingBox(translateDirection(howsIt.facingDirection));
					// ModLog.info(entity.getName() + " " + coord.toString());
				}
				this.world.spawnEntityInWorld(entity);
			} catch (final Exception t) {
				ModLog.warn("Unable to place entity");
			}
		}
	}

	protected void generateChestContents(final IInventory inventory, final String category, final int count) {
		final List<WeightedRandomChestContent> contents = ChestGenHooks.getItems(category, rand);
		if (contents == null || contents.size() == 0)
			ModLog.warn("Unable to get chest contents: %s", category);
		else
			WeightedRandomChestContent.generateChestContents(rand, contents, inventory, count);
	}

	protected boolean doFillChestContents(final IBlockState state) {
		return BlockHelper.isChest(state) && StringUtils.isNotEmpty(this.properties.chestContents);
	}

	protected boolean doSkipSpawnerPlacement(final IBlockState state, final BlockPos v) {
		if (BlockHelper.isSpawner(state) && rand.nextInt(100) >= this.properties.spawnerEnableChance) {
			this.blockList.add(v);
			return true;
		}
		return false;
	}

	protected boolean waitToPlace(final IBlockState state) {
		return BlockHelper.isTorch(state) || BlockHelper.isLever(state) || BlockHelper.isButton(state)
				|| BlockHelper.isDoor(state);
	}

	protected EnumFacing translateDirection(final EnumFacing dir) {
		final int count = getRotationCount(dir);
		switch (count) {
		case 1:
			return dir.rotateY();
		case 2:
			return dir.getOpposite();
		case 3:
			return dir.rotateYCCW();
		case 0:
		default:
			return dir;
		}
	}

	protected int getRotationCount(final EnumFacing dir) {
		int count = 0;
		if (this.orientation != null && dir != null) {
			if (this.orientation == EnumFacing.WEST || this.orientation == EnumFacing.EAST)
				count = 1;

			if ((this.orientation == EnumFacing.NORTH || this.orientation == EnumFacing.EAST)
					&& (dir == EnumFacing.NORTH || dir == EnumFacing.SOUTH))
				count += 2;
		}

		return count;
	}

	protected IBlockState handleRotation(final IBlockState state) {

		// Get it's current facing. If it is unknown
		// just return - it is not handled or it's a
		// basic block like dirt.
		final EnumFacing direction = BlockHelper.getOrientation(state);
		if (direction == null)
			return state;

		return BlockHelper.rotate(state, getRotationCount(direction));
	}
}
