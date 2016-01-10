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

package org.blockartistry.mod.Restructured.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.component.CopyStructureBuilder;
import org.blockartistry.mod.Restructured.component.IStructureBuilder;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Dimensions;
import org.blockartistry.mod.Restructured.world.RegionHelper.RegionStats;
import org.blockartistry.mod.Restructured.world.themes.BlockThemes;

public class SchematicWorldGenStructure implements IStructureBuilder {

	private static final IBlockState DIRT_BLOCK = Blocks.dirt.getDefaultState();
	private static final int VARIANCE_THRESHOLD = 3;
	private static final int WATER_RATIO_THRESHOLD = 35;

	protected final World world;
	protected EnumFacing direction;
	protected final SchematicProperties properties;
	protected StructureBoundingBox boundingBox;
	protected final BiomeGenBase biome;

	public SchematicWorldGenStructure(final World world, final BiomeGenBase biome, final EnumFacing direction,
			final int x, final int z, final SchematicProperties properties) {
		final Dimensions size = properties.schematic.getDimensions();
		this.world = world;
		this.direction = direction;
		this.properties = properties;
		this.biome = biome;
		this.boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, 1, z, 0, 0, 0, size.width, size.height,
				size.length, direction);
	}

	@Override
	public Dimensions getDimensions() {
		return properties.schematic.getDimensions();
	}

	@Override
	public boolean isVecInside(final BlockPos pos, final StructureBoundingBox box) {
		final BlockPos v = getWorldCoordinates(pos);
		return box.isVecInside(v);
	}

	@Override
	public BlockPos getWorldCoordinates(final BlockPos v) {
		return new BlockPos(this.getXWithOffset(v.getX(), v.getZ()), this.getYWithOffset(v.getY()),
				this.getZWithOffset(v.getX(), v.getZ()));
	}

	@Override
	public void placeBlock(final World world, final IBlockState state, final BlockPos v,
			final StructureBoundingBox box) {
		final BlockPos pos = getWorldCoordinates(v);
		if (box.isVecInside(pos)) {
			final IBlockState blockToPlace = BlockThemes.findReplacement(this.biome, state);
			world.setBlockState(pos, blockToPlace, 2);
		}
	}

	protected int getXWithOffset(final int x, final int z) {
		if (this.direction == null)
			return x;

		switch (this.direction) {
		case NORTH:
		case SOUTH:
			return this.boundingBox.minX + x;
		case WEST:
			return this.boundingBox.maxX - z;
		case EAST:
			return this.boundingBox.minX + z;
		default:
			return x;
		}
	}

	protected int getYWithOffset(final int y) {
		return this.direction == null ? y : y + this.boundingBox.minY;
	}

	protected int getZWithOffset(final int x, final int z) {
		if (this.direction == null)
			return z;

		switch (this.direction) {
		case SOUTH:
			return this.boundingBox.minZ + z;
		case EAST:
		case WEST:
			return this.boundingBox.minZ + x;
		case NORTH:
			return this.boundingBox.maxZ - z;
		default:
			return z;
		}
	}

	/**
	 * Deletes all continuous blocks from selected position upwards. Stops at
	 * hitting air.
	 */
	protected void clearUpwards(final int x, final int y, final int z, final StructureBoundingBox box) {
		final int l = getXWithOffset(x, z);
		final int j1 = getZWithOffset(x, z);
		final int i1 = getYWithOffset(y);

		BlockPos pos = new BlockPos(l, i1, j1);

		if (box.isVecInside(pos)) {
			while (!world.isAirBlock(pos) && i1 < pos.getY()) {
				world.setBlockState(pos, Blocks.air.getDefaultState(), 2);
				pos = pos.up();
			}
		}
	}

	protected void clearDownwards(final IBlockState state, final int x, final int y, final int z,
			final StructureBoundingBox box) {

		final int i1 = getXWithOffset(x, z);
		final int k1 = getZWithOffset(x, z);
		final int j1 = getYWithOffset(y);

		BlockPos pos = new BlockPos(i1, j1, k1);

		if (box.isVecInside(pos)) {

			do {
				final IBlockState t = world.getBlockState(pos);
				if (BlockHelper.isAir(t) || BlockHelper.isLiquid(t) || !BlockHelper.isSolid(t)) {
					world.setBlockState(pos, state, 2);
					pos = pos.down();
				} else
					break;
			} while (pos.getY() > 1);
		}
	}

	protected boolean prepare(final StructureBoundingBox box) {

		// This calculation can result in additional chunk load/creates.
		// The size of the structure could cause it to span chunks.
		final RegionStats stats = RegionHelper.getRegionStatsWithVariance(world, boundingBox);

		// If there is too much variance return false. Can't stand
		// structures on dirt pillars.
		if (stats.variance > VARIANCE_THRESHOLD)
			return false;

		// If there is too much surface water in the region return
		if (stats.water > 0 && (stats.area * 100) / stats.water > WATER_RATIO_THRESHOLD)
			return false;

		// Based on the terrain in the region adjust
		// the Y to an appropriate level
		final int offset = properties.groundOffset;
		boundingBox.offset(0, stats.mean - boundingBox.minY - offset, 0);
		ModLog.debug("WorldGen structure [%s] @(%s); mode %d", properties.name, boundingBox, direction);
		ModLog.debug(stats.toString());

		// Ensure a platform for the structure
		final IBlockState blockToPlace = BlockThemes.findReplacement(this.biome, DIRT_BLOCK);
		final Dimensions size = getDimensions();
		for (int xx = 0; xx < size.width; xx++) {
			for (int zz = 0; zz < size.length; zz++) {
				clearUpwards(xx, 0, zz, box);
				clearDownwards(blockToPlace, xx, -1, zz, box);
			}
		}

		return true;
	}

	public void build() {

		final StructureBoundingBox box = new StructureBoundingBox(boundingBox.minX, 1, boundingBox.minZ,
				boundingBox.maxX, 512, boundingBox.maxZ);

		if (!prepare(box))
			return;

		final CopyStructureBuilder builder = new CopyStructureBuilder(world, box, direction, properties, this);
		builder.generate();
	}
}
