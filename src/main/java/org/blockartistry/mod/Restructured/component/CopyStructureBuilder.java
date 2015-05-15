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

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.util.ForgeDirection;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.forge.RotationHelper;
import org.blockartistry.mod.Restructured.schematica.ISchematic;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Vector;

public class CopyStructureBuilder {

	static final Random rand = new Random();

	final IStructureBuilder structure;
	final World world;
	final StructureBoundingBox box;
	final int orientation;
	final SchematicProperties properties;

	ArrayList<Vector> waitToPlace = new ArrayList<Vector>();
	ArrayList<Vector> blockList = new ArrayList<Vector>();

	public CopyStructureBuilder(World world, StructureBoundingBox box,
			int orientation, SchematicProperties properties,
			IStructureBuilder structure) {

		this.world = world;
		this.box = box;
		this.orientation = orientation;
		this.structure = structure;
		this.properties = properties;
	}

	public void place(Block block, int meta, int x, int y, int z) {
		if (block == null)
			block = Blocks.air;
		structure.placeBlock(world, block, translateMeta(block, meta), x, y, z,
				box);
	}

	public boolean isVecInside(int x, int y, int z, StructureBoundingBox box) {
		return structure.isVecInside(x, y, z, box);
	}

	public void generate() {

		ISchematic schematic = properties.schematic;

		Vector size = structure.getDimensions();
		for (int x = 0; x < size.x; x++)
			for (int z = 0; z < size.z; z++)
				for (int y = 0; y < size.y; y++) {

					if (isVecInside(x, y, z, box)) {

						BlockHelper block = new BlockHelper(schematic.getBlock(
								x, y, z));
						Vector v = new Vector(x, y, z);

						// Do we skip placement?
						if (doSkipFireSource(block)
								|| doSkipSpawnerPlacement(block, v))
							continue;

						// Delay placing things that don't like being
						// rotated or attached to blocks that change
						if (waitToPlace(block))
							waitToPlace.add(v);
						else {

							int meta = schematic.getBlockMetadata(x, y, z);
							place(block.theBlock(), meta, x, y, z);
						}
					}
				}

		if (!waitToPlace.isEmpty()) {
			for (Vector v : waitToPlace) {
				int x = (int) v.x;
				int y = (int) v.y;
				int z = (int) v.z;
				Block block = schematic.getBlock(x, y, z);
				int meta = schematic.getBlockMetadata(x, y, z);
				place(block, meta, x, y, z);
			}
		}

		for (TileEntity e : schematic.getTileEntities()) {
			if (!isVecInside(e.xCoord, e.yCoord, e.zCoord, box))
				continue;

			// If the block location is black listed we don't want
			// to create the tile entity at the location
			if (blockList.contains(new Vector(e.xCoord, e.yCoord, e.zCoord)))
				continue;

			try {
				// Clone it - we don't want to use the master copy because
				// it will be used for other buildings.
				TileEntity entity = cloneTileEntity(e);

				// Update the entity with the proper state.
				BlockHelper helper = new BlockHelper(schematic.getBlock(
						entity.xCoord, entity.yCoord, entity.zCoord));

				entity.setWorldObj(world);
				entity.blockType = helper.theBlock();
				entity.blockMetadata = translateMeta(entity.blockType,
						schematic.getBlockMetadata(entity.xCoord,
								entity.yCoord, entity.zCoord));
				entity.validate();

				// Place it into the world
				Vector coord = structure.getWorldCoordinates(entity.xCoord,
						entity.yCoord, entity.zCoord);
				world.setTileEntity((int) coord.x, (int) coord.y,
						(int) coord.z, entity);

				if (doFillChestContents(helper)) {
					generateChestContents((IInventory) entity,
							properties.chestContents,
							properties.chestContentsCount);
				}

			} catch (Exception ex) {
				ModLog.warn("Unable to place TileEntity");
				ex.printStackTrace();
			}
		}

		for (Entity e : schematic.getEntities()) {
			int x = (int) e.posX;
			int y = (int) e.posY;
			int z = (int) e.posZ;

			if (!isVecInside(x, y, z, box))
				continue;

			try {
				// Place it into the world
				Vector coord = structure.getWorldCoordinates(x, y, z);
				Entity entity = cloneEntity(e);
				entity.setPosition(coord.x, coord.y, coord.z);

				world.spawnEntityInWorld(entity);
			} catch (Throwable t) {
				ModLog.warn("Unable to place entity");
			}
		}
	}

	void generateChestContents(IInventory inventory, String category, int count) {
		WeightedRandomChestContent[] contents = ChestGenHooks.getItems(
				category, rand);
		WeightedRandomChestContent.generateChestContents(rand, contents,
				inventory, count);
	}

	boolean doFillChestContents(BlockHelper helper) {
		return helper.isChest() && properties.chestContents != null
				&& !properties.chestContents.isEmpty();
	}

	boolean doSkipFireSource(BlockHelper helper) {
		return helper.isFireSource() && properties.suppressFire;
	}

	boolean doSkipSpawnerPlacement(BlockHelper helper, Vector v) {
		if (helper.isSpawner()
				&& rand.nextInt(100) >= properties.spawnerEnableChance) {
			blockList.add(v);
			return true;
		}
		return false;
	}

	boolean waitToPlace(BlockHelper block) {
		return block.isTorch();
	}

	int translateMeta(Block block, int meta) {

		ForgeDirection direction = RotationHelper.metadataToDirection(block,
				meta);
		if (direction == ForgeDirection.UNKNOWN) {
			return meta;
		}

		switch (orientation) {
		case 0:
			break;

		case 1:
			meta = RotationHelper.rotateVanillaBlock(block, meta,
					ForgeDirection.UP);
			break;

		case 3:
			meta = RotationHelper.rotateVanillaBlock(block, meta,
					ForgeDirection.UP);
			// fall through...
		case 2:
			if (direction == ForgeDirection.NORTH
					|| direction == ForgeDirection.SOUTH) {
				meta = RotationHelper.rotateVanillaBlock(block, meta,
						ForgeDirection.UP);
				meta = RotationHelper.rotateVanillaBlock(block, meta,
						ForgeDirection.UP);
			}
			break;

		default:
			return meta;
		}

		return meta;
	}

	TileEntity cloneTileEntity(TileEntity source) {
		NBTTagCompound nbt = new NBTTagCompound();
		source.writeToNBT(nbt);
		return TileEntity.createAndLoadEntity(nbt);
	}

	Entity cloneEntity(Entity entity) {
		NBTTagCompound nbt = new NBTTagCompound();
		entity.writeToNBTOptional(nbt);
		return EntityList.createEntityFromNBT(nbt, world);
	}
}
