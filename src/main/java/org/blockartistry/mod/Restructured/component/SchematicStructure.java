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

import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.schematica.ISchematic;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Dimensions;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;

public class SchematicStructure extends VillageStructureBase implements
		IStructureBuilder {

	protected SchematicProperties properties;
	protected BiomeGenBase biome;

	private static ChunkCoordinates fromSchematic(final ISchematic schem) {
		return new ChunkCoordinates(schem.getWidth(), schem.getHeight(), schem.getLength());
	}

	public SchematicStructure() {
		super(null, 0, null, null, 0);
	}

	public SchematicStructure(final Start start, final int type, final Random rand,
			final StructureBoundingBox box, final int direction) {
		super(start, type, rand, box, direction);
		
		this.biome = start.biome;
	}

	public void setProperties(final SchematicProperties props) {
		this.properties = props;
	}

	@Override
	protected void func_143012_a(final NBTTagCompound nbt) {
		super.func_143012_a(nbt);

		nbt.setString("key", properties.name);
		nbt.setInteger("biome", biome.biomeID);
	}

	@Override
	protected void func_143011_b(final NBTTagCompound nbt) {
		super.func_143011_b(nbt);

		final String temp = nbt.getString("key");
		if (temp != null)
			this.properties = Assets.getProperties(temp);
		
		// If the biome is Ocean it means it hasn't been set
		// Force it to plains.
		final int b = nbt.getInteger("biome");
		if(b == 0)
			biome = BiomeGenBase.plains;
		else
			biome = BiomeGenBase.getBiome(b);
	}

	@Override
	public Dimensions getDimensions() {
		return properties.schematic.getDimensions();
	}

	@Override
	public int getGroundOffset() {
		return properties.groundOffset;
	}

	public String getName() {
		return properties.name;
	}

	@Override
	public void build(final World world, final Random rand, final StructureBoundingBox box) {

		final CopyStructureBuilder builder = new CopyStructureBuilder(world, box,
				coordBaseMode, properties, this);
		builder.generate();
	}

	protected ChunkCoordinates getSafeVillagerLocation() {

		// Initialize starting point
		final ChunkCoordinates size = fromSchematic(properties.schematic);
		int x = size.posX >> 1;
		int z = size.posZ >> 1;
		int y = properties.groundOffset;

		// Try several times finding a suitable spot
		final ISchematic s = properties.schematic;
		for (int i = 0; i < 4; i++) {
			final BlockHelper block = new BlockHelper(s.getBlock(
					x, y + 1, z));
			if (block.canBreath()) {
				
				// Bump up one in case he is standing in wood
				// or something.
				if(s.getBlock(x, y, z) != Blocks.air)
					y += 1;
				
				break;
			}

			// No - won't cut it. Adjust.
			x += 1 - rand.nextInt(3);
			z += 1 - rand.nextInt(3);
		}

		return new ChunkCoordinates(x, y, z);
	}

	@Override
	public void spawnPeople(final World world, final StructureBoundingBox box) {

		int count = properties.villagerCount;
		if (count == -1)
			count = rand.nextInt(4);
		if (count == 0)
			return;

		final ChunkCoordinates loc = getSafeVillagerLocation();
		this.spawnVillagers(world, box, loc.posX, loc.posY, loc.posZ,
				count);
	}

	@Override
	protected int getVillagerType(final int p_74888_1_) {
		int type = properties.villagerProfession;
		if (type == -1)
			type = rand.nextInt(5);
		return type;
	}

	@Override
	public boolean isVecInside(final int x, final int y, final int z, final StructureBoundingBox box) {
		final ChunkCoordinates v = getWorldCoordinates(x, y, z);
		return box.isVecInside(v.posX, v.posY, v.posZ);
	}

	@Override
	public ChunkCoordinates getWorldCoordinates(final int x, final int y, final int z) {
		return new ChunkCoordinates(this.getXWithOffset(x, z), this.getYWithOffset(y),
				this.getZWithOffset(x, z));
	}

	@Override
	public ChunkCoordinates getWorldCoordinates(final ChunkCoordinates v) {
		return getWorldCoordinates(v.posX, v.posY, v.posZ);
	}
}
