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

package org.blockartistry.mod.Restructured.world.themes;

import org.blockartistry.mod.Restructured.util.BlockHelper;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

class ThemeBlock {

	protected Block block;
	protected int meta;

	protected ThemeBlock() {
		this(null, 0);
	}

	public ThemeBlock(final Block block) {
		this(block, 0);
	}

	public ThemeBlock(final Block block, final int meta) {
		this.block = block;
		this.meta = (block == Blocks.air) ? 0 : meta;
	}

	public Block getBlock() {
		return this.block;
	}

	public int getMeta() {
		return this.meta;
	}

	public boolean isSlab() {
		return BlockHelper.isSlab(this.block);
	}
	
	public boolean isLog() {
		return BlockHelper.isLog(this.block);
	}
}
