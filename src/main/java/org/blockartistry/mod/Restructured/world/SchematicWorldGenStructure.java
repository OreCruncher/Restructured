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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.component.CopyStructureBuilder;
import org.blockartistry.mod.Restructured.component.IStructureBuilder;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Dimensions;
import org.blockartistry.mod.Restructured.util.SelectedBlock;
import org.blockartistry.mod.Restructured.world.RegionHelper.RegionStats;
import org.blockartistry.mod.Restructured.world.themes.BlockThemes;

public class SchematicWorldGenStructure implements IStructureBuilder {

	protected static final SelectedBlock DIRT_BLOCK = new SelectedBlock(Blocks.dirt, 0);

	protected static final int VARIANCE_THRESHOLD = 3;

	protected final World world;
	protected int direction;
	protected final SchematicProperties properties;
	protected StructureBoundingBox boundingBox;
	protected final BiomeGenBase biome;

	public SchematicWorldGenStructure(final World world, final BiomeGenBase biome, final int direction, final int x,
			final int z, final SchematicProperties properties) {
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
	public boolean isVecInside(final int x, final int y, final int z, final StructureBoundingBox box) {
		final ChunkCoordinates v = getWorldCoordinates(x, y, z);
		return box.isVecInside(v.posX, v.posY, v.posZ);
	}

	@Override
	public ChunkCoordinates getWorldCoordinates(final int x, final int y, final int z) {
		return new ChunkCoordinates(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
	}

	@Override
	public ChunkCoordinates getWorldCoordinates(final ChunkCoordinates v) {
		return getWorldCoordinates(v.posX, v.posY, v.posZ);
	}

	@Override
	public void placeBlock(final World world, final SelectedBlock block, final int x, final int y, final int z,
			final StructureBoundingBox box) {

		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);

		if (box.isVecInside(i1, j1, k1)) {
			final SelectedBlock blockToPlace = BlockThemes.findReplacement(this.biome, block,
					properties.suppressMonsterEgg);
			world.setBlock(i1, j1, k1, blockToPlace.getBlock(), blockToPlace.getMeta(), 2);
		}
	}

	protected int getXWithOffset(final int x, final int z) {
		switch (this.direction) {
		case 0:
		case 2:
			return this.boundingBox.minX + x;
		case 1:
			return this.boundingBox.maxX - z;
		case 3:
			return this.boundingBox.minX + z;
		default:
			return x;
		}
	}

	protected int getYWithOffset(final int y) {
		return this.direction == -1 ? y : y + this.boundingBox.minY;
	}

	protected int getZWithOffset(final int x, final int z) {
		switch (this.direction) {
		case 0:
			return this.boundingBox.minZ + z;
		case 1:
		case 3:
			return this.boundingBox.minZ + x;
		case 2:
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
		int i1 = getYWithOffset(y);

		if (box.isVecInside(l, i1, j1)) {
			while (!world.isAirBlock(l, i1, j1) && i1 < 255) {
				world.setBlock(l, i1++, j1, Blocks.air, 0, 2);
			}
		}
	}

	protected void clearDownwards(final Block block, final int meta, final int x, final int y, final int z,
			final StructureBoundingBox box) {

		final int i1 = getXWithOffset(x, z);
		final int k1 = getZWithOffset(x, z);
		int j1 = getYWithOffset(y);

		if (box.isVecInside(i1, j1, k1)) {

			do {
				BlockHelper helper = new BlockHelper(world.getBlock(i1, j1, k1));
				if (helper.isAir() || helper.isLiquid() || !helper.isSolid())
					world.setBlock(i1, j1--, k1, block, meta, 2);
				else
					break;
			} while (j1 > 1);
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

		// Based on the terrain in the region adjust
		// the Y to an appropriate level
		final int offset = properties.groundOffset;
		boundingBox.offset(0, stats.mean - boundingBox.minY - offset, 0);
		ModLog.debug("WorldGen structure [%s] @(%s); mode %d", properties.name, boundingBox, direction);
		ModLog.debug(stats.toString());

		// Ensure a platform for the structure
		final SelectedBlock blockToPlace = BlockThemes.findReplacement(this.biome, DIRT_BLOCK,
				properties.suppressMonsterEgg);
		final Dimensions size = getDimensions();
		for (int xx = 0; xx < size.width; xx++) {
			for (int zz = 0; zz < size.length; zz++) {
				clearUpwards(xx, 0, zz, box);
				clearDownwards(blockToPlace.getBlock(), blockToPlace.getMeta(), xx, -1, zz, box);
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
