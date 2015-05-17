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
 * The majority of the source in this file was obtained from LatBlocks, an MIT
 * licensed open source project managed by Lunatrius.  You can find that project
 * at: https://github.com/Lunatrius/Schematica
 * 
 * This source file has been modified from the original to suit the purpose of
 * this project.  If you are looking to reuse this code it is heavily suggested
 * that the source be acquired from the LatBlocks source vault.
 */

package org.blockartistry.mod.Restructured.schematica;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.world.FantasyIsland;

public class SchematicAlpha extends SchematicFormat {
	private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData
			.getBlockRegistry();

	@Override
	public ISchematic readFromNBT(NBTTagCompound tagCompound) {

		byte localBlocks[] = tagCompound.getByteArray(Names.NBT.BLOCKS);
		byte localMetadata[] = tagCompound.getByteArray(Names.NBT.DATA);

		boolean extra = false;
		byte extraBlocks[] = null;
		byte extraBlocksNibble[] = null;
		if (tagCompound.hasKey(Names.NBT.ADD_BLOCKS)) {
			extra = true;
			extraBlocksNibble = tagCompound.getByteArray(Names.NBT.ADD_BLOCKS);
			extraBlocks = new byte[extraBlocksNibble.length * 2];
			for (int i = 0; i < extraBlocksNibble.length; i++) {
				extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
				extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
			}
		} else if (tagCompound.hasKey(Names.NBT.ADD_BLOCKS_SCHEMATICA)) {
			extra = true;
			extraBlocks = tagCompound
					.getByteArray(Names.NBT.ADD_BLOCKS_SCHEMATICA);
		}

		short width = tagCompound.getShort(Names.NBT.WIDTH);
		short length = tagCompound.getShort(Names.NBT.LENGTH);
		short height = tagCompound.getShort(Names.NBT.HEIGHT);

		Short id = null;
		Map<Short, Short> oldToNew = new HashMap<Short, Short>();
		if (tagCompound.hasKey(Names.NBT.MAPPING_SCHEMATICA)) {
			NBTTagCompound mapping = tagCompound
					.getCompoundTag(Names.NBT.MAPPING_SCHEMATICA);
			@SuppressWarnings("unchecked")
			Set<String> names = mapping.func_150296_c();
			for (String name : names) {
				oldToNew.put(mapping.getShort(name),
						(short) BLOCK_REGISTRY.getId(name));
			}
		}

		Schematic schematic = new Schematic(null, width, height, length);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					int index = x + (y * length + z) * width;
					int blockID = (localBlocks[index] & 0xFF)
							| (extra ? ((extraBlocks[index] & 0xFF) << 8) : 0);
					int meta = localMetadata[index] & 0xFF;

					if ((id = oldToNew.get((short) blockID)) != null) {
						blockID = id;
					}

					schematic.setBlock(x, y, z,
							BLOCK_REGISTRY.getObjectById(blockID), meta);
				}
			}
		}

		NBTTagList tileEntitiesList = tagCompound.getTagList(
				Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
			try {
				TileEntity tileEntity = TileEntity
						.createAndLoadEntity(tileEntitiesList
								.getCompoundTagAt(i));
				if (tileEntity != null) {
					schematic.setTileEntity(tileEntity.xCoord,
							tileEntity.yCoord, tileEntity.zCoord, tileEntity);
				}
			} catch (Exception e) {
				ModLog.error("TileEntity failed to load properly!", e);
			}
		}

		if (tagCompound.hasKey("WEOriginX") && tagCompound.hasKey("WEOriginY")
				&& tagCompound.hasKey("WEOriginZ")) {

			// Get WorldEdit origin information so we can offset the entities
			// properly.
			int originX = tagCompound.getInteger("WEOriginX");
			int originY = tagCompound.getInteger("WEOriginY");
			int originZ = tagCompound.getInteger("WEOriginZ");

			NBTTagList entitiesList = tagCompound.getTagList(
					Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);
			
			for (int i = 0; i < entitiesList.tagCount(); i++) {
				try {
					NBTTagCompound cp = entitiesList.getCompoundTagAt(i);
					Entity entity = EntityList.createEntityFromNBT(cp, FantasyIsland.instance);
					if (entity != null && !(entity instanceof EntityItemFrame)) {

						entity.posX = entity.posX - originX;
						entity.posY = entity.posY - originY;
						entity.posZ = entity.posZ - originZ;

						schematic.addEntity(entity);
					}
				} catch (Exception e) {
					ModLog.error("Entity failed to load properly!", e);
				}
			}
		}

		return schematic;
	}
}
