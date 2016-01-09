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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class SelectedBlock implements Cloneable {

	protected Block block;
	protected int meta;

	protected SelectedBlock() {
		this(null, 0);
	}

	public SelectedBlock(final Block block) {
		this(block, 0);
	}

	public SelectedBlock(final Block block, final int meta) {
		this.block = block;
		this.meta = (block == Blocks.air) ? 0 : meta;
	}

	public SelectedBlock(final IBlockState state) {
		this(state.getBlock(), state.getBlock().getMetaFromState(state));
	}
	
	public Block getBlock() {
		return this.block;
	}

	public int getMeta() {
		return this.meta;
	}

	public IBlockState getBlockState() {
		return this.block.getStateFromMeta(this.meta);
	}
	
	public boolean isSlab() {
		return BlockHelper.isSlab(this.block);
	}
	
	public boolean isLog() {
		return BlockHelper.isLog(this.block);
	}

	@Override
	public Object clone() {
		return new SelectedBlock(this.block, this.meta);
	}
}
