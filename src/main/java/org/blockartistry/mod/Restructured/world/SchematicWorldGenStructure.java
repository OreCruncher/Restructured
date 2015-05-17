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

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.component.CopyStructureBuilder;
import org.blockartistry.mod.Restructured.component.IStructureBuilder;
import org.blockartistry.mod.Restructured.math.BoxHelper;
import org.blockartistry.mod.Restructured.math.BoxHelper.RegionStats;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Tuple;
import org.blockartistry.mod.Restructured.util.Vector;

import cpw.mods.fml.common.eventhandler.Event.Result;

public class SchematicWorldGenStructure implements IStructureBuilder {

	protected static final HashMap<Block, Tuple<Block,Integer>> desertReplacement = new HashMap<Block,Tuple<Block,Integer>>();

	static {
		desertReplacement.put(Blocks.log, new Tuple<Block,Integer>(Blocks.sandstone, 0));
		desertReplacement.put(Blocks.log2, new Tuple<Block,Integer>(Blocks.sandstone, 0));
		desertReplacement.put(Blocks.cobblestone, new Tuple<Block,Integer>(Blocks.sandstone, 0));
		desertReplacement.put(Blocks.planks, new Tuple<Block,Integer>(Blocks.sandstone, 2));
		desertReplacement.put(Blocks.oak_stairs, new Tuple<Block,Integer>(Blocks.sandstone, -1));
		desertReplacement.put(Blocks.stone_stairs, new Tuple<Block,Integer>(Blocks.sandstone, -1));
		desertReplacement.put(Blocks.gravel, new Tuple<Block,Integer>(Blocks.sandstone, -1));
	}

	protected static Block findReplacementBlock(BiomeGenBase biome, Block block, int meta) {
		Tuple<Block, Integer> replace = null;
		
		if(biome == BiomeGenBase.desert || biome == BiomeGenBase.desertHills)
			replace = desertReplacement.get(block);

		return (replace == null) ? block : replace.val1;
	}
	
	protected static int findReplacementMeta(BiomeGenBase biome, Block block, int meta) {
		Tuple<Block, Integer> replace = null;
		
		if(biome == BiomeGenBase.desert || biome == BiomeGenBase.desertHills)
			replace = desertReplacement.get(block);
		
		return (replace == null) ? meta : (replace.val2 == -1 ? meta : replace.val2); 
	}
	
	protected final World world;
	protected final int direction;
	protected final SchematicProperties properties;
	protected StructureBoundingBox boundingBox;
	protected final BiomeGenBase biome;

	public SchematicWorldGenStructure(World world, BiomeGenBase biome,
			int direction, int x, int z, SchematicProperties properties) {
		Vector size = new Vector(properties.schematic);
		this.world = world;
		this.direction = direction;
		this.properties = properties;
		this.biome = biome;
		this.boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x,
				1, z, 0, 0, 0, (int) size.x, (int) size.y, (int) size.z,
				direction);
	}

	@Override
	public Vector getDimensions() {
		return new Vector(properties.schematic);
	}

	@Override
	public boolean isVecInside(int x, int y, int z, StructureBoundingBox box) {
		Vector v = getWorldCoordinates(x, y, z);
		return box.isVecInside((int) v.x, (int) v.y, (int) v.z);
	}

	@Override
	public Vector getWorldCoordinates(int x, int y, int z) {
		return new Vector(this.getXWithOffset(x, z), this.getYWithOffset(y),
				this.getZWithOffset(x, z));
	}

	@Override
	public Vector getWorldCoordinates(double x, double y, double z) {
		return getWorldCoordinates((int) x, (int) y, (int) z);
	}

	@Override
	public Vector getWorldCoordinates(Vector v) {
		return getWorldCoordinates(v.x, v.y, v.z);
	}

	@Override
	public void placeBlock(World world, Block block, int meta, int x, int y,
			int z, StructureBoundingBox box) {

		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);

		if (box.isVecInside(i1, j1, k1)) {
			Block blockToPlace = convertBlockForBiome(block, meta);
			int newMeta = convertBlockMetadata(block, meta);
			world.setBlock(i1, j1, k1, blockToPlace, newMeta, 2);
		}
	}

	protected int getXWithOffset(int x, int z) {
		switch (this.direction) {
		case 0:
		case 2:
			return this.boundingBox.minX + x;
		case 1:
			return this.boundingBox.maxX - z;
		case 3:
			return this.boundingBox.minX + z;
		default:
			return x;
		}
	}

	protected int getYWithOffset(int y) {
		return this.direction == -1 ? y : y + this.boundingBox.minY;
	}

	protected int getZWithOffset(int x, int z) {
		switch (this.direction) {
		case 0:
			return this.boundingBox.minZ + z;
		case 1:
		case 3:
			return this.boundingBox.minZ + x;
		case 2:
			return this.boundingBox.maxZ - z;
		default:
			return z;
		}
	}

	protected Block convertBlockForBiome(Block block, int meta) {

		if(properties.suppressMonsterEgg) {
			BlockHelper helper = new BlockHelper(block);
			Tuple<Block, Integer> result = helper.getNonMonsterEgg(meta);
			block = result.val1;
			meta = result.val2;
		}

		BiomeEvent.GetVillageBlockID event = new BiomeEvent.GetVillageBlockID(
				biome, block, meta);
		MinecraftForge.TERRAIN_GEN_BUS.post(event);
		if (event.getResult() == Result.DENY)
			return event.replacement;
		
		return findReplacementBlock(biome, block, meta);
	}

	protected int convertBlockMetadata(Block block, int meta) {

		if(properties.suppressMonsterEgg) {
			BlockHelper helper = new BlockHelper(block);
			Tuple<Block, Integer> result = helper.getNonMonsterEgg(meta);
			block = result.val1;
			meta = result.val2;
		}

		BiomeEvent.GetVillageBlockMeta event = new BiomeEvent.GetVillageBlockMeta(
				biome, block, meta);
		MinecraftForge.TERRAIN_GEN_BUS.post(event);
		if (event.getResult() == Result.DENY)
			return event.replacement;

		return findReplacementMeta(biome, block, meta);
	}

	/**
	 * Deletes all continuous blocks from selected position upwards. Stops at
	 * hitting air.
	 */
	protected void clearUpwards(int x, int y, int z, StructureBoundingBox box) {
		int l = getXWithOffset(x, z);
		int i1 = getYWithOffset(y);
		int j1 = getZWithOffset(x, z);

		if (box.isVecInside(l, i1, j1)) {
			while (!world.isAirBlock(l, i1, j1) && i1 < 255) {
				world.setBlock(l, i1, j1, Blocks.air, 0, 2);
				++i1;
			}
		}
	}

	protected void clearDownwards(Block block, int meta, int x, int y, int z,
			StructureBoundingBox box) {
		
		Block blockToPlace = convertBlockForBiome(block, meta);
		int newMeta = convertBlockMetadata(block, meta);

		int i1 = getXWithOffset(x, z);
		int j1 = getYWithOffset(y);
		int k1 = getZWithOffset(x, z);

		if (box.isVecInside(i1, j1, k1)) {
			while ((world.isAirBlock(i1, j1, k1) || world.getBlock(i1, j1, k1)
					.getMaterial().isLiquid())
					&& j1 > 1) {
				world.setBlock(i1, j1, k1, blockToPlace, newMeta, 2);
				--j1;
			}
		}
	}

	protected void prep(StructureBoundingBox box) {

		RegionStats stats = BoxHelper.getRegionStats(world, boundingBox,
				boundingBox);
		Vector size = getDimensions();
		int offset = properties.groundOffset;

		// Based on the terrain in the region adjust
		// the Y to an appropriate level
		boundingBox.offset(0, stats.mean - boundingBox.minY - offset, 0);

		// Ensure a platform for the structure
		for (int xx = 0; xx < size.x; xx++) {
			for (int zz = 0; zz < size.z; zz++) {
				clearUpwards(xx, 0, zz, box);
				clearDownwards(Blocks.grass, 0, xx, -1, zz, box);
			}
		}
	}

	public void build() {
		
		StructureBoundingBox box = new StructureBoundingBox(boundingBox.minX,
				1, boundingBox.minZ, boundingBox.maxX, 512, boundingBox.maxZ);
		ModLog.debug("WorldGen structure [%s] @(%s); mode %d", properties.name, boundingBox, direction);

		prep(box);
		CopyStructureBuilder builder = new CopyStructureBuilder(world, box,
				direction, properties, this);
		builder.generate();
	}
}
