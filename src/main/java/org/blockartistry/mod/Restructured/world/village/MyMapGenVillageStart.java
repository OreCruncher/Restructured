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

package org.blockartistry.mod.Restructured.world.village;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;

public class MyMapGenVillageStart extends StructureStart {

    /** well ... thats what it does */
    private boolean hasMoreThanTwoComponents;

    public MyMapGenVillageStart() {}
    
    @SuppressWarnings("unchecked")
	public MyMapGenVillageStart(World world, Random random, int chunkX, int chunkZ, int terrainType)
    {
        super(chunkX, chunkZ);
        List<PieceWeight> list = VillagePieces.getStructureVillageWeightedPieceList(random, terrainType);
        VillagePieces.MyStart start = new VillagePieces.MyStart(world.getWorldChunkManager(), 0, random, (chunkX << 4) + 2, (chunkZ << 4) + 2, list, terrainType);
        this.components.add(start);
        start.buildComponent(start, this.components, random);
        List<?> list1 = start.field_74930_j;
        List<?> list2 = start.field_74932_i;
        int l;

        while (!list1.isEmpty() || !list2.isEmpty())
        {
            StructureComponent structurecomponent;

            if (list1.isEmpty())
            {
                l = random.nextInt(list2.size());
                structurecomponent = (StructureComponent)list2.remove(l);
                structurecomponent.buildComponent(start, this.components, random);
            }
            else
            {
                l = random.nextInt(list1.size());
                structurecomponent = (StructureComponent)list1.remove(l);
                structurecomponent.buildComponent(start, this.components, random);
            }
        }

        this.updateBoundingBox();
        l = 0;
        Iterator<?> iterator = this.components.iterator();

        while (iterator.hasNext())
        {
            StructureComponent structurecomponent1 = (StructureComponent)iterator.next();

            if (!(structurecomponent1 instanceof StructureVillagePieces.Road))
            {
                ++l;
            }
        }

        this.hasMoreThanTwoComponents = l > 2;
    }

    /**
     * currently only defined for Villages, returns true if Village has more than 2 non-road components
     */
    public boolean isSizeableStructure()
    {
        return this.hasMoreThanTwoComponents;
    }

    public void func_143022_a(NBTTagCompound p_143022_1_)
    {
        super.func_143022_a(p_143022_1_);
        p_143022_1_.setBoolean("Valid", this.hasMoreThanTwoComponents);
    }

    public void func_143017_b(NBTTagCompound p_143017_1_)
    {
        super.func_143017_b(p_143017_1_);
        this.hasMoreThanTwoComponents = p_143017_1_.getBoolean("Valid");
    }
}