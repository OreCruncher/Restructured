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

package org.blockartistry.mod.Restructured.util;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

public class SelectedBlock extends BlockHelper implements Cloneable {

	private static final PropertyDirection FACING = PropertyDirection.create("facing");

	protected int meta;

	protected SelectedBlock() {
		this(null, 0);
	}

	public SelectedBlock(final Block block) {
		this(block, 0);
	}

	public SelectedBlock(final Block block, final int meta) {
		super(block);
		this.meta = (block == Blocks.air) ? 0 : meta;
	}

	public SelectedBlock(final IBlockState state) {
		this(state.getBlock(), state.getBlock().getMetaFromState(state));
	}

	public int getMeta() {
		return this.meta;
	}

	public void rotate(final int count) {

		if (count == 0)
			return;

		// Use our Fantasy to satisfy Minecraft so we can rotate
		// the block without having to place it into the world.
		// Should expose methods that don't rely on a world instance. :\
		EnumFacing current = getOrientation();
		if (current == null || current == EnumFacing.UP || current == EnumFacing.DOWN)
			return;

		for (int i = 0; i < count; i++)
			current = current.rotateY();

		final IBlockState updated = getBlockState().withProperty(FACING, current);
		this.meta = updated.getBlock().getMetaFromState(updated);
	}

	public EnumFacing getOrientation() {
		final IBlockState state = getBlockState();
		if (state.getPropertyNames().contains(FACING))
			return state.getValue(FACING);
		return null;
	}

	public IBlockState getBlockState() {
		return this.block.getStateFromMeta(this.meta);
	}

	@Override
	public Object clone() {
		return new SelectedBlock(this.block, this.meta);
	}
}
