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
import net.minecraft.init.Blocks;

public class BlockHelper {
	
	final Block block;
	
	public BlockHelper(Block block) {
		this.block = (block != null) ? block : Blocks.air;
	}
	
	public Block theBlock() {
		return block;
	}
	
	public boolean isAir() {
		return block == Blocks.air;
	}
	
	public boolean canBreath() {
		return isAir() || !block.getMaterial().isSolid();
	}
	
	public boolean isChest() {
		return block == Blocks.chest;
	}
	
	public boolean isLava() {
		return block == Blocks.lava || block == Blocks.flowing_lava;
	}
	
	public boolean isWater() {
		return block == Blocks.water || block == Blocks.flowing_water;
	}
	
	public boolean isFire() {
		return block == Blocks.fire;
	}
	
	public boolean isFireSource() {
		return isFire() || isLava();
	}
	
	public boolean isTorch() {
		return block == Blocks.torch || block == Blocks.redstone_torch;
	}

}
