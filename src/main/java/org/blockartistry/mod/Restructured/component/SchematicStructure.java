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
import org.blockartistry.mod.Restructured.schematica.Schematic;
import org.blockartistry.mod.Restructured.util.BlockHelper;
import org.blockartistry.mod.Restructured.util.Dimensions;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;

public class SchematicStructure extends VillageStructureBase implements
		IStructureBuilder {

	protected SchematicProperties properties;
	protected BiomeGenBase biome;

	public SchematicStructure() {
		super(null, 0, null, null, EnumFacing.NORTH);
	}

	public SchematicStructure(final Start start, final int type, final Random rand,
			final StructureBoundingBox box, final EnumFacing direction) {
		super(start, type, rand, box, direction);
		
		this.biome = start.biome;
	}

	public void setProperties(final SchematicProperties props) {
		this.properties = props;
	}

	@Override
	protected void writeStructureToNBT(final NBTTagCompound nbt) {
		super.writeStructureToNBT(nbt);

		nbt.setString("key", properties.name);
		nbt.setInteger("biome", biome.biomeID);
	}

	@Override
	protected void readStructureFromNBT(final NBTTagCompound nbt) {
		super.readStructureFromNBT(nbt);

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

	protected BlockPos getSafeVillagerLocation() {

		// Initialize starting point
		final Dimensions size = properties.schematic.getDimensions();
		int x = size.width >> 1;
		int z = size.length >> 1;
		int y = properties.groundOffset;

		BlockPos pos = new BlockPos(x, y + 1, z);

		// Try several times finding a suitable spot
		final Schematic s = properties.schematic;
		for (int i = 0; i < 4; i++) {
			
			final IBlockState state = s.getBlockState(pos);
			if (BlockHelper.canBreath(state)) {
				break;
			}

			// No - won't cut it. Adjust.
			pos = pos.add(1 - rand.nextInt(3), 0, 1 - rand.nextInt(3));
		}

		return pos.down();
	}

	@Override
	public void spawnPeople(final World world, final StructureBoundingBox box) {

		int count = properties.villagerCount;
		if (count == -1)
			count = rand.nextInt(4);
		if (count == 0)
			return;

		final BlockPos loc = getSafeVillagerLocation();
		this.spawnVillagers(world, box, loc.getX(), loc.getY(), loc.getZ(),
				count);
	}

	@Override
	protected int func_180779_c(final int count, final int proposedProfession) {
		int type = properties.villagerProfession;
		if (type == -1)
			type = rand.nextInt(5);
		return type;
	}

	@Override
	public boolean isVecInside(final BlockPos pos, final StructureBoundingBox box) {
		final BlockPos v = getWorldCoordinates(pos);
		return box.isVecInside(v);
	}

	@Override
	public BlockPos getWorldCoordinates(final BlockPos pos) {
		return new BlockPos(this.getXWithOffset(pos.getX(), pos.getZ()), this.getYWithOffset(pos.getY()),
				this.getZWithOffset(pos.getX(), pos.getZ()));
	}
}
