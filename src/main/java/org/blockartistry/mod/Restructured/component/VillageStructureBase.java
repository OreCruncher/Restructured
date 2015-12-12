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

import org.blockartistry.mod.Restructured.util.Dimensions;
import org.blockartistry.mod.Restructured.util.SelectedBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

public abstract class VillageStructureBase extends StructureVillagePieces.Village {

	protected static final Random rand = new Random();

	protected int averageGroundLevel = -1;

	public VillageStructureBase(final StructureVillagePieces.Start start, final int componentType, final Random random,
			final StructureBoundingBox myBox, final int orientation) {
		super(start, componentType);
		this.coordBaseMode = orientation;
		this.boundingBox = myBox;
	}

	public abstract Dimensions getDimensions();

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
	public void placeBlock(final World world, final SelectedBlock block, final int x, final int y, final int z,
			final StructureBoundingBox box) {
		placeBlockAtCurrentPosition(world, block.getBlock(), block.getMeta(), x, y, z, box);
	}

	public int getMetaWithOffset(final Block block, final int meta) {
		return getMetadataWithOffset(block, meta);
	}

	@Override
	public boolean addComponentParts(final World world, final Random rand, final StructureBoundingBox box) {

		final Dimensions size = getDimensions();

		if (this.field_143015_k < 0) {
			this.field_143015_k = this.getAverageGroundLevel(world, box);
			if (this.field_143015_k < 0)
				return true;
			boundingBox.offset(0, this.field_143015_k - boundingBox.maxY
					+ size.height - getGroundOffset() - 1, 0);
		}

		// Ensure a platform for the structure
		for (int xx = 0; xx < size.width; xx++) {
			for (int zz = 0; zz < size.length; zz++) {
				clearCurrentPositionBlocksUpwards(world, xx, 0, zz, box);
				func_151554_b(world, Blocks.dirt, 0, xx, -1, zz, box);
			}
		}

		build(world, rand, box);
		spawnPeople(world, box);
		return true;
	}

	public abstract void build(final World world, final Random rand, final StructureBoundingBox box);

	public void spawnPeople(final World world, final StructureBoundingBox box) {
		// Base implementation does nothing - override to create
		// villagers for the structure
	}
}
