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
import org.blockartistry.mod.Restructured.util.SelectedBlock;

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

	public void place(final SelectedBlock block, final int x, final int y, final int z) {
		handleRotation(block);
		structure.placeBlock(world, block, x, y, z, box);
	}

	public boolean isVecInside(final BlockPos pos, final StructureBoundingBox box) {
		return structure.isVecInside(pos.getX(), pos.getY(), pos.getZ(), box);
	}

	public boolean isVecInside(final int x, final int y, final int z, final StructureBoundingBox box) {
		return structure.isVecInside(x, y, z, box);
	}

	public void generate() {

		final Schematic schematic = properties.schematic;
		final Dimensions size = structure.getDimensions();

		// Scan from the ground up. This is important when
		// replacing crops.
		for (int y = 0; y < size.height; y++)
			for (int x = 0; x < size.width; x++)
				for (int z = 0; z < size.length; z++) {

					if (isVecInside(x, y, z, box)) {

						final SelectedBlock block = schematic.getBlockEx(x, y, z);
						final BlockPos v = new BlockPos(x, y, z);

						// Do we skip placement?
						if (doSkipSpawnerPlacement(block, v))
							continue;

						// Delay placing things that don't like being
						// rotated or attached to blocks that change
						if (waitToPlace(block)) {
							waitToPlace.add(v);
							continue;
						}

						place(block, x, y, z);
					}
				}

		if (!waitToPlace.isEmpty()) {
			for (final BlockPos v : waitToPlace) {
				final SelectedBlock block = schematic.getBlockEx(v.getX(), v.getY(), v.getZ());
				place(block, v.getX(), v.getY(), v.getZ());
			}
		}

		for (final SchematicTileEntity e : schematic.getTileEntities()) {
			final BlockPos coords = e.coords;
			if (!isVecInside(coords.getX(), coords.getY(), coords.getZ(), box))
				continue;

			// If the block location is black listed we don't want
			// to create the tile entity at the location
			if (blockList.contains(coords))
				continue;

			try {
				final TileEntity entity = (TileEntity) e.getInstance(world);
				entity.validate();

				// Update the entity with the proper state.
				final BlockHelper helper = new BlockHelper(schematic.getBlock(entity.getPos()));

				// Place it into the world
				final BlockPos coord = structure.getWorldCoordinates(entity.getPos());
				world.setTileEntity(coord, entity);

				if (doFillChestContents(helper)) {
					generateChestContents((IInventory) entity, properties.chestContents, properties.chestContentsCount);
				}

			} catch (Exception ex) {
				ModLog.warn("Unable to place TileEntity");
				ex.printStackTrace();
			}
		}

		for (final SchematicEntity e : schematic.getEntities()) {
			final BlockPos ec = e.coords;

			if (!isVecInside(ec, box))
				continue;

			try {
				final Entity entity = (Entity) e.getInstance(world);

				// TODO: Looks like another mess
				/*
				 * if (entity instanceof EntityHanging) { final EntityHanging
				 * howsIt = (EntityHanging) entity; final BlockPos coord =
				 * structure.getWorldCoordinates(howsIt.getPosition());
				 * howsIt.setPosition(coord.getX(), coord.getY(), coord.getZ());
				 * // Calls setPosition() internally // TODO: This is a direct
				 * assignment - not sure if the // entity // is properly updated
				 * to hang howsIt.facingDirection =
				 * translateDirection(howsIt.facingDirection); } else {
				 */

				final BlockPos coord = structure.getWorldCoordinates(ec);
				final double spawnX = coord.getX();// + entity.posX % 1;
				final double spawnY = coord.getY();// + entity.posY % 1;
				final double spawnZ = coord.getZ();// + entity.posZ % 1;
				entity.setPosition(spawnX, spawnY, spawnZ);

				if (entity instanceof EntityHanging) {
					final EntityHanging howsIt = (EntityHanging) entity;
					howsIt.updateFacingWithBoundingBox(translateDirection(howsIt.facingDirection));
					ModLog.info(entity.getName() + " x:" + spawnX + ", y:" + spawnY + ", z:" + spawnZ);
				}
				world.spawnEntityInWorld(entity);
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

	protected boolean doFillChestContents(final BlockHelper helper) {
		return helper.isChest() && StringUtils.isNotEmpty(properties.chestContents);
	}

	protected boolean doSkipSpawnerPlacement(final BlockHelper helper, final BlockPos v) {
		if (helper.isSpawner() && rand.nextInt(100) >= properties.spawnerEnableChance) {
			blockList.add(v);
			return true;
		}
		return false;
	}

	protected boolean waitToPlace(final BlockHelper block) {
		return block.isTorch() || block.isLever() || block.isButton() || block.isDoor();
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

	protected void handleRotation(final SelectedBlock block) {

		// Get it's current facing. If it is unknown
		// just return - it is not handled or it's a
		// basic block like dirt.
		final EnumFacing direction = block.getOrientation();
		if (direction == null)
			return;

		block.rotate(getRotationCount(direction));
	}
}
