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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.blockartistry.mod.Restructured.util.Dimensions;
import org.blockartistry.mod.Restructured.world.themes.BlockThemes;

public class Schematic {
	
	private static final IBlockState AIR = Blocks.air.getDefaultState();

	public static class SchematicTileEntity {

		public final BlockPos coords;
		public final NBTTagCompound nbt;

		public SchematicTileEntity(final NBTTagCompound nbt, final BlockPos pos) {
			this.coords = new BlockPos(pos);
			this.nbt = nbt;
		}

		public Object getInstance(final World world) {
			return TileEntity.createAndLoadEntity(nbt);
		}
	}

	public static class SchematicEntity extends SchematicTileEntity {

		public final UUID id;

		public SchematicEntity(final UUID id, final NBTTagCompound nbt, final BlockPos pos) {
			super(nbt, pos);
			this.id = id;
		}

		@Override
		public Object getInstance(final World world) {
			return EntityList.createEntityFromNBT(nbt, world);
		}
	}

	private final IBlockState[] data;

	private final List<SchematicTileEntity> tileEntities = new ArrayList<SchematicTileEntity>();
	private final List<SchematicEntity> entities = new ArrayList<SchematicEntity>();
	private final int width;
	private final int height;
	private final int length;

	private final int widthOffset;
	private final int heightOffset;

	private final Dimensions dim;

	public Schematic(final int width, final int height, final int length) {

		this.data = new IBlockState[width * height * length];

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

	private int getDataIndex(final BlockPos pos) {
		return pos.getX() * this.widthOffset + pos.getY() * this.heightOffset + pos.getZ();
	}

	public IBlockState getBlockState(final BlockPos pos) {
		if (!isValid(pos))
			return AIR;
		return this.data[getDataIndex(pos)];
	}

	public boolean setBlockState(final BlockPos pos, final IBlockState state) {
		if (!isValid(pos))
			return false;
		this.data[getDataIndex(pos)] = state;
		return true;
	}

	public List<SchematicTileEntity> getTileEntities() {
		return this.tileEntities;
	}

	public void addTileEntity(final BlockPos pos, final NBTTagCompound nbt) {
		if (!isValid(pos)) {
			return;
		}

		this.removeTileEntity(pos);

		if (nbt != null) {
			this.tileEntities.add(new SchematicTileEntity(nbt, pos));
		}
	}

	public void removeTileEntity(final BlockPos pos) {
		final Iterator<SchematicTileEntity> iterator = this.tileEntities.iterator();

		while (iterator.hasNext()) {
			final BlockPos coord = iterator.next().coords;
			if (coord.equals(pos))
				iterator.remove();
		}
	}

	public List<SchematicEntity> getEntities() {
		return this.entities;
	}

	public void addEntity(final UUID id, final NBTTagCompound nbt, final BlockPos pos) {
		for (final SchematicEntity e : this.entities) {
			if (id.equals(e.id)) {
				return;
			}
		}

		this.entities.add(new SchematicEntity(id, nbt, pos));
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

	private boolean isValid(final BlockPos pos) {
		return !(pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() >= this.width
				|| pos.getY() >= this.height || pos.getZ() >= this.length);
	}
}
