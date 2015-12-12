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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSeeds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.util.ForgeDirection;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.schematica.ISchematic;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Dimensions;
import org.blockartistry.mod.Restructured.util.SelectedBlock;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class CopyStructureBuilder {

	protected static final Random rand = new Random();

	private static Field soilBlockID = null;
	private static Field cropBlock = null;

	static {
		try {
			soilBlockID = ReflectionHelper.findField(ItemSeeds.class, "soilBlockID", "field_77838_b");
			cropBlock = ReflectionHelper.findField(ItemSeeds.class, "field_150925_a");
		} catch (final Exception t) {
			ModLog.warn("Unable to hook ItemSeeds.soilBlockId");
		}
	}

	protected final IStructureBuilder structure;
	protected final World world;
	protected final StructureBoundingBox box;
	protected final int orientation;
	protected final SchematicProperties properties;

	protected final List<ChunkCoordinates> waitToPlace = new ArrayList<ChunkCoordinates>();
	protected final List<ChunkCoordinates> blockList = new ArrayList<ChunkCoordinates>();

	public CopyStructureBuilder(final World world, final StructureBoundingBox box, final int orientation,
			final SchematicProperties properties, final IStructureBuilder structure) {

		this.world = world;
		this.box = box;
		this.orientation = orientation;
		this.structure = structure;
		this.properties = properties;
	}

	public void place(final Block block, final int meta, final int x, final int y, final int z) {
		final SelectedBlock b = SelectedBlock.fly(block, meta);
		handleRotation(b);
		structure.placeBlock(world, b, x, y, z, box);
	}

	public boolean isVecInside(final int x, final int y, final int z, final StructureBoundingBox box) {
		return structure.isVecInside(x, y, z, box);
	}

	public void generate() {

		final ISchematic schematic = properties.schematic;
		final Dimensions size = structure.getDimensions();

		// Scan from the ground up. This is important when
		// replacing crops.
		for (int y = 0; y < size.height; y++)
			for (int x = 0; x < size.width; x++)
				for (int z = 0; z < size.length; z++) {

					if (isVecInside(x, y, z, box)) {

						BlockHelper block = new BlockHelper(schematic.getBlock(x, y, z));
						final ChunkCoordinates v = new ChunkCoordinates(x, y, z);

						// Do we skip placement?
						if (doSkipFireSource(block) || doSkipSpawnerPlacement(block, v))
							continue;

						// Delay placing things that don't like being
						// rotated or attached to blocks that change
						if (waitToPlace(block)) {
							waitToPlace.add(v);
							continue;
						}

						// If this is a crop, and we are randomizing crops,
						// need to do the appropriate replacement.
						int meta = 0;
						Block replacement = replaceCrop(block, x, y, z);
						if (replacement != null) {
							block = new BlockHelper(replacement);
							meta = rand.nextInt(7);
						} else {
							meta = schematic.getBlockMetadata(x, y, z);
						}

						// Fall through case - just place the block
						place(block.getBlock(), meta, x, y, z);
					}
				}

		if (!waitToPlace.isEmpty()) {
			for (final ChunkCoordinates v : waitToPlace) {
				final Block block = schematic.getBlock(v.posX, v.posY, v.posZ);
				final int meta = schematic.getBlockMetadata(v.posX, v.posY, v.posZ);
				place(block, meta, v.posX, v.posY, v.posZ);
			}
		}

		for (final TileEntity e : schematic.getTileEntities()) {
			if (!isVecInside(e.xCoord, e.yCoord, e.zCoord, box))
				continue;

			// If the block location is black listed we don't want
			// to create the tile entity at the location
			if (blockList.contains(new ChunkCoordinates(e.xCoord, e.yCoord, e.zCoord)))
				continue;

			try {
				// Clone it - we don't want to use the master copy because
				// it will be used for other buildings.
				final TileEntity entity = cloneTileEntity(e);
				entity.validate();

				// Update the entity with the proper state.
				final BlockHelper helper = new BlockHelper(
						schematic.getBlock(entity.xCoord, entity.yCoord, entity.zCoord));

				// Place it into the world
				final ChunkCoordinates coord = structure.getWorldCoordinates(entity.xCoord, entity.yCoord,
						entity.zCoord);
				world.setTileEntity(coord.posX, coord.posY, coord.posZ, entity);

				if (doFillChestContents(helper)) {
					generateChestContents((IInventory) entity, properties.chestContents, properties.chestContentsCount);
				}

			} catch (Exception ex) {
				ModLog.warn("Unable to place TileEntity");
				ex.printStackTrace();
			}
		}

		for (final Entity e : schematic.getEntities()) {
			final int x = (int) Math.floor(e.posX);
			final int y = (int) Math.floor(e.posY);
			final int z = (int) Math.floor(e.posZ);

			if (!isVecInside(x, y, z, box))
				continue;

			try {
				// Place it into the world
				final Entity entity = cloneEntity(e);

				if (entity instanceof EntityHanging) {
					final EntityHanging howsIt = (EntityHanging) entity;
					final ChunkCoordinates coord = structure.getWorldCoordinates(howsIt.field_146063_b,
							howsIt.field_146064_c, howsIt.field_146062_d);
					howsIt.field_146063_b = coord.posX;
					howsIt.field_146064_c = coord.posY;
					howsIt.field_146062_d = coord.posZ;
					// Calls setPosition() internally
					howsIt.setDirection(translateDirection(howsIt.hangingDirection));
				} else {
					final ChunkCoordinates coord = structure.getWorldCoordinates(x, y, z);
					entity.setPosition(coord.posX + 0.5D, coord.posY + 0.5D, coord.posZ + 0.5D);
				}

				world.spawnEntityInWorld(entity);
			} catch (final Exception t) {
				ModLog.warn("Unable to place entity");
			}
		}
	}

	protected Block replaceCrop(final BlockHelper block, final int x, final int y, final int z) {

		if (!properties.randomizeCrops || !block.isCrop())
			return null;

		final List<ItemSeeds> seeds = Assets.getSeeds();
		if (seeds == null || seeds.size() == 0)
			return null;

		Block result = null;
		try {
			// Get our new seed and what it can be planted on
			final ItemSeeds s = seeds.get(rand.nextInt(seeds.size()));
			final Block newHostBlock = (Block) soilBlockID.get(s);

			// If the block at y-1 can support, yay! If not
			// return null.
			if (properties.schematic.getBlock(x, y - 1, z) == newHostBlock)
				result = (Block) cropBlock.get(s);

		} catch (final Exception t) {
			;
		}

		return result;
	}

	protected void generateChestContents(final IInventory inventory, final String category, final int count) {
		final WeightedRandomChestContent[] contents = ChestGenHooks.getItems(category, rand);
		if (contents == null || contents.length == 0)
			ModLog.warn("Unable to get chest contents: %s", category);
		else
			WeightedRandomChestContent.generateChestContents(rand, contents, inventory, count);
	}

	protected boolean doFillChestContents(final BlockHelper helper) {
		return helper.isChest() && properties.chestContents != null && !properties.chestContents.isEmpty();
	}

	protected boolean doSkipFireSource(final BlockHelper helper) {
		return helper.isFireSource() && properties.suppressFire;
	}

	protected boolean doSkipSpawnerPlacement(final BlockHelper helper, final ChunkCoordinates v) {
		if (helper.isSpawner() && rand.nextInt(100) >= properties.spawnerEnableChance) {
			blockList.add(v);
			return true;
		}
		return false;
	}

	protected boolean waitToPlace(final BlockHelper block) {
		return block.isTorch() || block.isLever() || block.isButton() || block.isDoor();
	}

	protected int translateDirection(final int dir) {

		final int count = getRotationCount(dir);
		if (count == 1)
			return Direction.rotateRight[dir];
		if (count == 2)
			return Direction.rotateOpposite[dir];
		if (count == 3)
			return Direction.rotateLeft[dir];

		return dir;
	}

	protected int getRotationCount(final int dir) {
		switch (dir) {
		case 0:
			return getRotationCount(ForgeDirection.SOUTH);
		case 1:
			return getRotationCount(ForgeDirection.WEST);
		case 2:
			return getRotationCount(ForgeDirection.NORTH);
		case 4:
			return getRotationCount(ForgeDirection.EAST);
		default:
			;
		}
		return -1;
	}

	protected int getRotationCount(final ForgeDirection dir) {

		int rotationCount = 0;
		if (orientation == 1 || orientation == 3)
			rotationCount++;

		if ((orientation == 2 || orientation == 3) && (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH))
			rotationCount += 2;

		return rotationCount;
	}

	protected void handleRotation(final SelectedBlock block) {

		// If the block is in natural position
		// just return.
		if (orientation == 0)
			return;

		// Get it's current facing. If it is unknown
		// just return - it is not handled or it's a
		// basic block like dirt.
		final ForgeDirection direction = block.getOrientation();
		if (direction == ForgeDirection.UNKNOWN || direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) {
			return;
		}

		block.rotate(ForgeDirection.UP, getRotationCount(direction));
	}

	protected TileEntity cloneTileEntity(final TileEntity source) {
		final NBTTagCompound nbt = new NBTTagCompound();
		source.writeToNBT(nbt);
		return TileEntity.createAndLoadEntity(nbt);
	}

	protected Entity cloneEntity(final Entity entity) {
		final NBTTagCompound nbt = new NBTTagCompound();
		entity.writeToNBTOptional(nbt);
		return EntityList.createEntityFromNBT(nbt, world);
	}
}
