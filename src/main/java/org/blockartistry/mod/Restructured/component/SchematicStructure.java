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

import java.util.ArrayList;
import java.util.Random;

import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;
import org.blockartistry.mod.Restructured.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;

public class SchematicStructure extends VillageStructureBase {

	static final ArrayList<SchematicProperties> schematics = new ArrayList<SchematicProperties>();

	SchematicProperties properties;

	public SchematicStructure() {
		super(null, EAST, null, null, EAST);
	}

	public SchematicStructure(Start start, int type, Random rand,
			StructureBoundingBox box, int direction) {
		super(start, type, rand, box, direction);
	}

	public void setProperties(SchematicProperties props) {
		this.properties = props;
	}

	@Override
	protected void func_143012_a(NBTTagCompound nbt) {
		super.func_143012_a(nbt);

		nbt.setString("key", properties.name);
	}

	@Override
	protected void func_143011_b(NBTTagCompound nbt) {
		super.func_143011_b(nbt);

		String temp = nbt.getString("key");
		if (temp != null)
			this.properties = Assets.getProperties(temp);
	}

	@Override
	public Vector getDimensions() {
		return new Vector(properties.schematic);
	}

	@Override
	public int getGroundOffset() {
		return properties.groundOffset;
	}

	public String getName() {
		return properties.name;
	}

	@Override
	public void build(World world, Random rand, StructureBoundingBox box) {

		CopyStructureBuilder builder = new CopyStructureBuilder(world, box,
				coordBaseMode, properties.suppressFire, this);
		builder.place(properties.schematic);
	}

	@Override
	public void spawnPeople(World world, StructureBoundingBox box) {

		int count = properties.villagerCount;
		if (count == -1)
			count = new Random().nextInt(4);
		if (count == 0)
			return;

		Vector size = new Vector(properties.schematic);
		int x = size.x / 2;
		int z = size.z / 2;
		int y = properties.groundOffset;
		
		this.spawnVillagers(world, box, x, y, z, count);
	}

	@Override
	protected int getVillagerType(int p_74888_1_) {
		int type = properties.villagerProfession;
		if (type == -1)
			type = new Random().nextInt(5);
		return type;
	}

}
