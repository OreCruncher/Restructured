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

import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.blockartistry.mod.Restructured.util.Dimensions;
import org.blockartistry.mod.Restructured.util.SelectedBlock;
import org.blockartistry.mod.Restructured.world.themes.BlockThemes;

public class Schematic {

	public static class SchematicTileEntity {

		public final ChunkCoordinates coords;
		public final NBTTagCompound nbt;

		public SchematicTileEntity(final NBTTagCompound nbt, final int x, final int y, final int z) {
			this.coords = new ChunkCoordinates(x, y, z);
			this.nbt = nbt;
		}
		
		public Object getInstance(final World world) {
			return TileEntity.createAndLoadEntity(nbt);
		}
	}
	
	public static class SchematicEntity extends SchematicTileEntity {
		
		public final UUID id;
		
		public SchematicEntity(final UUID id, final NBTTagCompound nbt, final int x, final int y, final int z) {
			super(nbt, x, y, z);
			this.id = id;
		}
		
		@Override
		public Object getInstance(final World world) {
			return EntityList.createEntityFromNBT(nbt, world);
		}
	}

	private final SelectedBlock[] data;

	private final List<SchematicTileEntity> tileEntities = new ArrayList<SchematicTileEntity>();
	private final List<SchematicEntity> entities = new ArrayList<SchematicEntity>();
	private final int width;
	private final int height;
	private final int length;

	private final int widthOffset;
	private final int heightOffset;

	private final Dimensions dim;

	public Schematic(final int width, final int height, final int length) {

		this.data = new SelectedBlock[width * height * length];

		this.width = width;
		this.height = height;
		this.length = length;

		this.dim = new Dimensions(width, height, length);

		// int[dimX][dimY][dimZ] : 1-D array index [i * dimY*dimZ + j * dimZ +
		// k]
		this.widthOffset = height * length;
		this.heightOffset = length;
	}

	public void scrubFireSources() {
		for (int i = 0; i < this.data.length; i++)
			this.data[i] = BlockThemes.scrubFireSource(this.data[i]);
	}

	public void scrubEggs() {
		for (int i = 0; i < this.data.length; i++)
			this.data[i] = BlockThemes.scrubEggs(this.data[i]);
	}

	public Dimensions getDimensions() {
		return this.dim;
	}

	protected int getDataIndex(int x, int y, int z) {
		return x * this.widthOffset + y * this.heightOffset + z;
	}

	public SelectedBlock getBlockEx(final int x, final int y, final int z) {
		if (!isValid(x, y, z))
			return new SelectedBlock(Blocks.air);
		return (SelectedBlock) this.data[getDataIndex(x, y, z)].clone();
	}

	public Block getBlock(final int x, final int y, final int z) {
		if (!isValid(x, y, z))
			return Blocks.air;
		return this.data[getDataIndex(x, y, z)].getBlock();
	}

	public boolean setBlock(final int x, final int y, final int z, final Block block, final int metadata) {
		if (!isValid(x, y, z))
			return false;
		this.data[getDataIndex(x, y, z)] = new SelectedBlock(block, metadata);
		return true;
	}

	public SchematicTileEntity getTileEntity(final int x, final int y, final int z) {
		for (final SchematicTileEntity entry : this.tileEntities) {
			final ChunkCoordinates coords = entry.coords;
			if (coords.posX == x && coords.posY == y && coords.posZ == z) {
				return entry;
			}
		}

		return null;
	}

	public List<SchematicTileEntity> getTileEntities() {
		return this.tileEntities;
	}

	public void addTileEntity(final int x, final int y, final int z, final NBTTagCompound nbt) {
		if (!isValid(x, y, z)) {
			return;
		}

		this.removeTileEntity(x, y, z);

		if (nbt != null) {
			this.tileEntities.add(new SchematicTileEntity(nbt, x, y, z));
		}
	}

	public void removeTileEntity(final int x, final int y, final int z) {
		final Iterator<SchematicTileEntity> iterator = this.tileEntities.iterator();

		while (iterator.hasNext()) {
			final ChunkCoordinates coord = iterator.next().coords;
			if (coord.posX == x && coord.posY == y && coord.posZ == z)
				iterator.remove();
		}
	}

	public int getBlockMetadata(final int x, final int y, final int z) {
		if (!isValid(x, y, z)) {
			return 0;
		}

		return this.data[getDataIndex(x, y, z)].getMeta();
	}

	public List<SchematicEntity> getEntities() {
		return this.entities;
	}

	public void addEntity(final UUID id, final NBTTagCompound nbt, final int x, final int y, final int z) {
		for (final SchematicEntity e : this.entities) {
			if (id.equals(e.id)) {
				return;
			}
		}

		this.entities.add(new SchematicEntity(id, nbt, x, y, z));
	}

	public int getWidth() {
		return this.width;
	}

	public int getLength() {
		return this.length;
	}

	public int getHeight() {
		return this.height;
	}

	private boolean isValid(final int x, final int y, final int z) {
		return !(x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
	}
}
