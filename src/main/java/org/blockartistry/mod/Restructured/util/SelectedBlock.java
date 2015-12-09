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

import org.blockartistry.mod.Restructured.world.FantasyIsland;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.RotationHelper;

public class SelectedBlock extends BlockHelper {
	
	// The cached key is used by the various framework routines where a temporary
	// key is generated just to index an internal table.  It's thread local so
	// there should be no collision.  They key should not be cached or used in
	// an index - unpredictable results will occur.
	private static final ThreadLocal<SelectedBlock> flyweight = new ThreadLocal<SelectedBlock>() {
        @Override
		protected SelectedBlock initialValue() {
            return new SelectedBlock();
        }
	};
	
	public static SelectedBlock fly(final Block block, final int meta) {
		final SelectedBlock t = flyweight.get();
		t.block = block;
		t.meta = meta;
		return t;
	}

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
	
	public int getMeta() {
		return this.meta;
	}
	
	public void rotate(final ForgeDirection axis, final int count) {
		// Use our Fantasy to satisfy Minecraft so we can rotate
		// the block without having to place it into the world.
		// Should expose methods that don't rely on a world instance. :\
		FantasyIsland.instance.meta = this.meta;
		for(int i = 0; i < count; i++)
			RotationHelper.rotateVanillaBlock(this.block, FantasyIsland.instance, 0, 0, 0, axis);
		this.meta = FantasyIsland.instance.meta;
	}
	
	public ForgeDirection getOrientation() {
		return BlockType.getOrientation(this);
	}
}
