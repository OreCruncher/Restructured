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

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import org.blockartistry.mod.Restructured.util.SelectedBlock;
import org.blockartistry.mod.Restructured.util.Vector;

public interface IStructureBuilder {

	/**
	 * Gets the dimensions of the structure. Dimensions have been translated for
	 * direction.
	 * 
	 * @return
	 */
	Vector getDimensions();

	/**
	 * Determines if the location is within the bounding boxes of the region.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param boundingBox
	 * @return
	 */
	boolean isVecInside(int x, int y, int z, StructureBoundingBox box);

	/**
	 * Gets the translated real world coordinates for the given location.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	Vector getWorldCoordinates(int x, int y, int z);
	Vector getWorldCoordinates(Vector v);

	/**
	 * Places a block into the world. The underlying logic is responsible for
	 * translating locations before setting.
	 * 
	 * @param world
	 * @param block
	 * @param meta
	 * @param x
	 * @param y
	 * @param z
	 * @param boundingBox
	 */
	void placeBlock(World world, SelectedBlock block, int x, int y, int z,
			StructureBoundingBox box);

}
