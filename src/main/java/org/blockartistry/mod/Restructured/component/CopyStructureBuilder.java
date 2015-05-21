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
import org.blockartistry.mod.Restructured.util.BlockRotationHelper;
import org.blockartistry.mod.Restructured.util.Vector;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class CopyStructureBuilder {

	static final Random rand = new Random();
	
	static Field soilBlockID = null;
	static Field cropBlock = null;
	
	static {
		
		try {
			
			soilBlockID = ReflectionHelper.findField(ItemSeeds.class, "soilBlockID", "field_77838_b");
			cropBlock = ReflectionHelper.findField(ItemSeeds.class, "field_150925_a");
			
		} catch(Throwable t) {
			
			ModLog.warn("Unable to hook ItemSeeds.soilBlockId");
			
			;
		}
	}
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
		structure.placeBlock(world, block, translateMeta(block, meta), x, y, z,
				box);
	}

	public boolean isVecInside(int x, int y, int z, StructureBoundingBox box) {
		return structure.isVecInside(x, y, z, box);
	}

	public void generate() {

		final ISchematic schematic = properties.schematic;
		final Vector size = structure.getDimensions();
		
		// Scan from the ground up.  This is important when
		// replacing crops.
		for (int y = 0; y < size.y; y++)
			for (int x = 0; x < size.x; x++)
				for (int z = 0; z < size.z; z++) {

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
						if (waitToPlace(block)) {
							waitToPlace.add(v);
							continue;
						}

						// If this is a crop, and we are randomizing crops,
						// need to do the appropriate replacement.
						int meta = 0;
						Block replacement = replaceCrop(block, x, y, z);
						if( replacement != null) {
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
				entity.validate();

				// Update the entity with the proper state.
				BlockHelper helper = new BlockHelper(schematic.getBlock(
						entity.xCoord, entity.yCoord, entity.zCoord));

				// Place it into the world
				Vector coord = structure.getWorldCoordinates(entity.xCoord,
						entity.yCoord, entity.zCoord);
				world.setTileEntity(coord.x, coord.y,
						coord.z, entity);

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
			int x = (int) Math.floor(e.posX);
			int y = (int) Math.floor(e.posY);
			int z = (int) Math.floor(e.posZ);

			if (!isVecInside(x, y, z, box))
				continue;

			try {
				// Place it into the world
				Entity entity = cloneEntity(e);

				if(entity instanceof EntityHanging) {
					EntityHanging howsIt = (EntityHanging) entity;
					Vector coord = structure.getWorldCoordinates(howsIt.field_146063_b, howsIt.field_146064_c, howsIt.field_146062_d);
					howsIt.field_146063_b = coord.x;
					howsIt.field_146064_c = coord.y;
					howsIt.field_146062_d = coord.z;
					// Calls setPosition() internally
					howsIt.setDirection(translateDirection(howsIt.hangingDirection));
				} else {
					Vector coord = structure.getWorldCoordinates(x, y, z);
					entity.setPosition(coord.x + 0.5D, coord.y + 0.5D, coord.z + 0.5D);
				}

				world.spawnEntityInWorld(entity);
			} catch (Throwable t) {
				ModLog.warn("Unable to place entity");
			}
		}
	}
	
	Block replaceCrop(BlockHelper block, int x, int y, int z) {
		if(!properties.randomizeCrops || !block.isCrop())
			return null;
		
		List<ItemSeeds> seeds = Assets.getSeeds();
		if(seeds == null || seeds.size() == 0)
			return null;
		
		Block result = null;
		try {
			// Get our new seed and what it can be planted on
			ItemSeeds s = seeds.get(rand.nextInt(seeds.size()));
			Block newHostBlock = (Block) soilBlockID.get(s);
			
			// If the block at y-1 can support, yay!  If not
			// return null.
			if(properties.schematic.getBlock(x, y - 1, z) == newHostBlock)
				result = (Block)cropBlock.get(s);
			
		} catch(Throwable t) {
			;
		}
		
		return result;
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

	int translateDirection(int dir) {
	
		int count = getRotationCount(dir);
		if(count == 1)
			return Direction.rotateRight[dir];
		if(count == 2)
			return Direction.rotateOpposite[dir];
		if(count == 3)
			return Direction.rotateLeft[dir];
		
		return dir;
	}
	
	int getRotationCount(int dir) {
		switch(dir) {
		case 0: return getRotationCount(ForgeDirection.SOUTH);
		case 1: return getRotationCount(ForgeDirection.WEST);
		case 2: return getRotationCount(ForgeDirection.NORTH);
		case 4: return getRotationCount(ForgeDirection.EAST);
		default:
			;
		}
		return -1;
	}
	
	int getRotationCount(ForgeDirection dir) {
		int rotationCount = 0;
		if(orientation == 1 || orientation == 3)
			rotationCount++;
		
		if((orientation == 2 || orientation == 3) && (dir == ForgeDirection.NORTH
					|| dir == ForgeDirection.SOUTH))
			rotationCount += 2;
		
		return rotationCount;
	}
	
	int translateMeta(Block block, int meta) {
		
		// If the block is in natural position
		// just return.
		if(orientation == 0)
			return meta;

		// Get it's current facing.  If it is unknown
		// just return - it is not handled or it's a 
		// basic block like dirt.
		ForgeDirection direction = BlockRotationHelper.metadataToDirection(block,
				meta);
		if (direction == ForgeDirection.UNKNOWN || direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) {
			return meta;
		}
		
		int rotationCount = getRotationCount(direction);
		
		meta = BlockRotationHelper.rotateVanillaBlock(block, meta,
				ForgeDirection.UP, rotationCount);

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
