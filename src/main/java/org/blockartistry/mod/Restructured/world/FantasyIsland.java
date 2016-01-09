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

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * This is a bare minimum world to satisfy the underlying entity spawn logic
 * of Minecraft/Forge.  Some entities, like horses, cannot be loaded from NBT
 * unless there is a world object.  Because at the time of schematic load
 * there isn't a world one has to be artificially provided.
 */
public class FantasyIsland extends World {

	// Ordering is important...
	private static final WorldProvider provider = new WorldProvider() {
		@Override
		public String getDimensionName() {
			return "Restructured Dummy Provider";
		}
		@Override
		public String getInternalNameSuffix() {
			return "RDP";
		}
	};

	public static final FantasyIsland instance = new FantasyIsland();
	
	public FantasyIsland() {
        super(new SaveHandler(), null, provider, null, false);
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return null;
	}

	@Override
	public Entity getEntityByID(int p_73045_1_) {
		return null;
	}
	
	@Override
	protected int getRenderDistanceChunks() {
		return 0;
	}
}
