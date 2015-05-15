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
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public interface ISchematic {
	/**
	 * Gets a block at a given location within the schematic. Requesting a block
	 * outside of those bounds returns Air.
	 *
	 * @param x
	 *            the X coord in world space.
	 * @param y
	 *            the Y coord in world space.
	 * @param z
	 *            the Z coord in world space.
	 * @return the block at the requested location.
	 */
	Block getBlock(int x, int y, int z);

	/**
	 * Gets the Tile Entity at the requested location. If no tile entity exists
	 * at that location, null will be returned.
	 *
	 * @param x
	 *            the X coord in world space.
	 * @param y
	 *            the Y coord in world space.
	 * @param z
	 *            the Z coord in world space.
	 * @return the located tile entity.
	 */
	TileEntity getTileEntity(int x, int y, int z);

	/**
	 * Returns a list of all tile entities in the schematic.
	 *
	 * @return all tile entities.
	 */
	List<TileEntity> getTileEntities();

	/**
	 * Gets the metadata of the block at the requested location.
	 *
	 * @param x
	 *            the X coord in world space.
	 * @param y
	 *            the Y coord in world space.
	 * @param z
	 *            the Z coord in world space.
	 * @return the Metadata Value
	 */
	int getBlockMetadata(int x, int y, int z);

	/**
	 * Returns a list of all entities in the schematic.
	 *
	 * @return all entities.
	 */
	List<Entity> getEntities();

	/**
	 * The width of the schematic
	 *
	 * @return the schematic width
	 */
	int getWidth();

	/**
	 * The length of the schematic
	 *
	 * @return the schematic length
	 */
	int getLength();

	/**
	 * The height of the schematic
	 *
	 * @return the schematic height
	 */
	int getHeight();
}
