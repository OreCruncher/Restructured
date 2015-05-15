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

import java.util.Random;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.math.BoxHelper;
import org.blockartistry.mod.Restructured.math.BoxHelper.RegionStats;
import org.blockartistry.mod.Restructured.util.Vector;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

public abstract class VillageStructureBase extends
		StructureVillagePieces.Village {

	protected static final Random rand = new Random();

	public VillageStructureBase(StructureVillagePieces.Start p_i2102_1_,
			int p_i2102_2_, Random p_i2102_3_, StructureBoundingBox p_i2102_4_,
			int p_i2102_5_) {
		super(p_i2102_1_, p_i2102_2_);
		this.coordBaseMode = p_i2102_5_;
		this.boundingBox = p_i2102_4_;
	}

	public abstract Vector getDimensions();

	public abstract int getGroundOffset();

	/**
	 * Public exposure of the placeBlockAtCurrentPosition() routine in a base
	 * class.
	 * 
	 * @param world
	 * @param block
	 * @param meta
	 * @param x
	 * @param y
	 * @param z
	 * @param boundingBox
	 */
	public void placeBlock(World world, Block block, int meta, int x, int y,
			int z, StructureBoundingBox box) {
		placeBlockAtCurrentPosition(world, block, meta, x, y, z, box);
	}

	public int getMetaWithOffset(Block block, int meta) {
		return getMetadataWithOffset(block, meta);
	}

	@Override
	public boolean addComponentParts(World world, Random rand,
			StructureBoundingBox box) {

		if (this.field_143015_k < 0) {
			RegionStats stats = BoxHelper.getRegionStats(world, box,
					boundingBox);
			ModLog.info(stats.toString());
			this.field_143015_k = stats.mean;

			if (stats.mean < 0)
				return true;
		}

		Vector size = getDimensions();
		int offset = getGroundOffset();

		boundingBox.offset(0, this.field_143015_k - boundingBox.maxY
				+ (int) size.y - offset - 1, 0);

		// Ensure a platform for the structure
		for (int xx = 0; xx < size.x; xx++) {
			for (int zz = 0; zz < size.z; zz++) {
				clearCurrentPositionBlocksUpwards(world, xx, 0, zz, box);
				func_151554_b(world, Blocks.grass, 0, xx, -1, zz, box);
			}
		}

		build(world, rand, box);
		spawnPeople(world, box);
		return true;
	}

	public abstract void build(World world, Random rand,
			StructureBoundingBox box);

	public void spawnPeople(World world, StructureBoundingBox box) {
		// Base implementation does nothing - override to create
		// villagers for the structure
	}
}
