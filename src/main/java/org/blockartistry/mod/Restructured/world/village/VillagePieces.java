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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.blockartistry.mod.Restructured.ModLog;
import org.blockartistry.mod.Restructured.util.SelectedBlock;
import org.blockartistry.mod.Restructured.world.village.themes.VillageTheme;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Path;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class VillagePieces {
	
	private static Field boundingBox = null;
	private static Field averageGroundLevel = null;
	
	static {
	
		try {
			boundingBox = ReflectionHelper.findField(StructureComponent.class, "boundingBox", "field_74887_e");
			averageGroundLevel = ReflectionHelper.findField(Path.class, "averageGroundLevel", "field_74934_a");
		} catch(final Exception t) {
			ModLog.warn("Unable to hook fields in VillagePieces");
		}
		
		MapGenStructureIO.func_143031_a(MyStart.class, "reViStart");
		MapGenStructureIO.func_143031_a(MyPath.class, "reViPath");
	}
	
	public static List<PieceWeight> getStructureVillageWeightedPieceList(
			final Random random, final int terrainType) {
		final ArrayList<PieceWeight> arrayList = new ArrayList<PieceWeight>();

		VillagerRegistry.addExtraVillageComponents(arrayList, random,
				terrainType);

		final Iterator<?> iterator = arrayList.iterator();

		while (iterator.hasNext()) {
			if (((StructureVillagePieces.PieceWeight) iterator.next()).villagePiecesLimit == 0) {
				iterator.remove();
			}
		}

		return arrayList;
	}
	
    private static int func_75079_a(final List<PieceWeight> weightList)
    {
        boolean flag = false;
        int i = 0;
        
        for(PieceWeight pw: weightList) {
        	i += pw.villagePieceWeight;
            if (pw.villagePiecesLimit > 0 && pw.villagePiecesSpawned < pw.villagePiecesLimit) {
                flag = true;
            }
        }

        return flag ? i : -1;
    }

    private static StructureVillagePieces.Village func_75083_a(final StructureVillagePieces.Start start, final StructureVillagePieces.PieceWeight pieceWeight, final List<StructureComponent> componentList, final Random random, final int x, final int y, final int z, final int p_75083_7_, final int p_75083_8_)
    {
        return (StructureVillagePieces.Village)VillagerRegistry.getVillageComponent(pieceWeight, start , componentList, random, x, y, z, p_75083_7_, p_75083_8_);
    }

    @SuppressWarnings("unchecked")
	private static StructureVillagePieces.Village getNextVillageComponent(final StructureVillagePieces.Start start, final List<StructureComponent> componentList, final Random random, final int x, final int y, final int z, final int p_75081_6_, final int componentType)
    {
        int j1 = func_75079_a(start.structureVillageWeightedPieceList);

        if (j1 <= 0)
        {
            return null;
        }
        else
        {
            int k1 = 0;

            while (k1 < 5)
            {
                ++k1;
                int l1 = random.nextInt(j1);
                Iterator<PieceWeight> iterator = start.structureVillageWeightedPieceList.iterator();

                while (iterator.hasNext())
                {
                    StructureVillagePieces.PieceWeight pieceweight = iterator.next();
                    l1 -= pieceweight.villagePieceWeight;

                    if (l1 < 0)
                    {
                        if (!pieceweight.canSpawnMoreVillagePiecesOfType(componentType) || pieceweight == start.structVillagePieceWeight && start.structureVillageWeightedPieceList.size() > 1)
                        {
                            break;
                        }

                        StructureVillagePieces.Village village = func_75083_a(start, pieceweight, componentList, random, x, y, z, p_75081_6_, componentType);

                        if (village != null)
                        {
                            ++pieceweight.villagePiecesSpawned;
                            start.structVillagePieceWeight = pieceweight;

                            if (!pieceweight.canSpawnMoreVillagePieces())
                            {
                                start.structureVillageWeightedPieceList.remove(pieceweight);
                            }

                            return village;
                        }
                    }
                }
            }
            
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	private static StructureComponent getNextVillageStructureComponent(final StructureVillagePieces.Start start, final List<StructureComponent> componentList, final Random random, final int x, final int y, final int z, final int p_75077_6_, final int p_75077_7_)
    {
        if (p_75077_7_ > 50)
        {
            return null;
        }
        else if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112)
        {
            StructureVillagePieces.Village village = getNextVillageComponent(start, componentList, random, x, y, z, p_75077_6_, p_75077_7_ + 1);

            if (village != null)
            {
            	try {
	            	StructureBoundingBox box = (StructureBoundingBox) VillagePieces.boundingBox.get(village);
	                int j1 = (box.minX + box.maxX) / 2;
	                int k1 = (box.minZ + box.maxZ) / 2;
	                int l1 = box.maxX - box.minX;
	                int i2 = box.maxZ - box.minZ;
	                int j2 = l1 > i2 ? l1 : i2;
	
	                if (start.getWorldChunkManager().areBiomesViable(j1, k1, j2 / 2 + 4, MapGenVillage.villageSpawnBiomes))
	                {
	                    componentList.add(village);
	                    start.field_74932_i.add(village);
	                    return village;
	                }
            	} catch(final Exception t) {
            		;
            	}
            }

            return null;
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	private static StructureComponent getNextComponentVillagePath(final MyStart start, final List<StructureComponent> componentList, final Random random, final int x, final int y, final int z, final int orientation, final int componentType)
    {
        if (componentType > 3 + start.terrainType)
        {
            return null;
        }
        else if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112)
        {
            StructureBoundingBox structureboundingbox = StructureVillagePieces.Path.func_74933_a(start, componentList, random, x, y, z, orientation);

            if (structureboundingbox != null && structureboundingbox.minY > 10)
            {
                MyPath path = new MyPath(start, componentType, random, structureboundingbox, orientation);
                StructureBoundingBox box = path.getBoundingBox();
                int j1 = (box.minX + box.maxX) / 2;
                int k1 = (box.minZ + box.maxZ) / 2;
                int l1 = box.maxX - box.minX;
                int i2 = box.maxZ - box.minZ;
                int j2 = l1 > i2 ? l1 : i2;

                if (start.getWorldChunkManager().areBiomesViable(j1, k1, j2 / 2 + 4, MapGenVillage.villageSpawnBiomes))
                {
                    componentList.add(path);
                    start.field_74930_j.add(path);
                    return path;
                }
            }

            return null;
        }
        else
        {
            return null;
        }
    }

	public static class MyStart extends StructureVillagePieces.Start {
		
		protected VillageTheme theme;
		
        public MyStart(final WorldChunkManager chunkManager, final int componentType, final Random random, final int x, final int z, final List<PieceWeight> weights, final int terrainType) {
        	super(chunkManager, componentType, random, x, z, weights, terrainType);
        	
        	theme = VillageTheme.find(this.biome);
        }

    	@Override
    	protected Block func_151558_b(final Block block, final int meta) {
    		// Completely override vanilla processing
    		return theme.findReplacementBlock(SelectedBlock.fly(block, meta), true);
    	}

    	@Override
        protected int func_151557_c(final Block block, final int meta) {
    		// Completely override vanilla processing
    		return theme.findReplacementMeta(SelectedBlock.fly(block, meta), true);
    	}

        // This plots out the well at the Village start
        @Override
        public boolean addComponentParts(final World world, final Random random, final StructureBoundingBox box)
        {
            if (this.field_143015_k < 0)
            {
                this.field_143015_k = this.getAverageGroundLevel(world, box);

                if (this.field_143015_k < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 3, 0);
            }

            this.fillWithBlocks(world, box, 1, 0, 1, 4, 12, 4, Blocks.cobblestone, Blocks.flowing_water, false);
            this.placeBlockAtCurrentPosition(world, Blocks.air, 0, 2, 12, 2, box);
            this.placeBlockAtCurrentPosition(world, Blocks.air, 0, 3, 12, 2, box);
            this.placeBlockAtCurrentPosition(world, Blocks.air, 0, 2, 12, 3, box);
            this.placeBlockAtCurrentPosition(world, Blocks.air, 0, 3, 12, 3, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 1, 13, 1, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 1, 14, 1, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 4, 13, 1, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 4, 14, 1, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 1, 13, 4, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 1, 14, 4, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 4, 13, 4, box);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 4, 14, 4, box);
            this.fillWithBlocks(world, box, 1, 15, 1, 4, 15, 4, Blocks.cobblestone, Blocks.cobblestone, false);

            for (int i = 0; i <= 5; ++i)
            {
                for (int j = 0; j <= 5; ++j)
                {
                    if (j == 0 || j == 5 || i == 0 || i == 5)
                    {
                        this.placeBlockAtCurrentPosition(world, Blocks.gravel, 0, j, 11, i, box);
                        this.clearCurrentPositionBlocksUpwards(world, j, 12, i, box);
                    }
                }
            }

            return true;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
		public void buildComponent(final StructureComponent start, final List componentList, final Random random)
        {
            getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.minX - 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, 1, this.getComponentType());
            getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.maxX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, 3, this.getComponentType());
            getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ - 1, 2, this.getComponentType());
            getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.maxZ + 1, 0, this.getComponentType());
        }
	}
	
	public static class MyPath extends StructureVillagePieces.Path {
		
		protected BiomeGenBase biome;
		protected VillageTheme theme;
		
        public MyPath(MyStart start, int componentType, Random random, StructureBoundingBox box, int orientation) {
        	super(start, componentType, random, box, orientation);
        	
        	this.biome = start.biome;
        	theme = start.theme;
        }
        
    	@Override
    	protected Block func_151558_b(Block block, int meta) {
    		// Completely override vanilla processing
    		return theme.findReplacementBlock(SelectedBlock.fly(block, meta), true);
    	}

    	@Override
        protected int func_151557_c(Block block, int meta) {
    		// Completely override vanilla processing
    		return theme.findReplacementMeta(SelectedBlock.fly(block, meta), true);
    	}

        /**
         * Initiates construction of the Structure Component picked, at the current Location of StructGen
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
		public void buildComponent(StructureComponent start, List componentList, Random random)
        {
            boolean flag = false;
            int i;
            StructureComponent structurecomponent1 = null;
            
            try {
	            int agl = averageGroundLevel.getInt(this);
	
	            for (i = random.nextInt(5); i < agl - 8; i += 2 + random.nextInt(5))
	            {
	                structurecomponent1 = this.getNextComponentNN((StructureVillagePieces.Start)start, componentList, random, 0, i);
	
	                if (structurecomponent1 != null)
	                {
	                	StructureBoundingBox b = (StructureBoundingBox) VillagePieces.boundingBox.get(structurecomponent1);
	                    i += Math.max(b.getXSize(), b.getZSize());
	                    flag = true;
	                }
	            }
	
	            for (i = random.nextInt(5); i < agl - 8; i += 2 + random.nextInt(5))
	            {
	                structurecomponent1 = this.getNextComponentPP((StructureVillagePieces.Start)start, componentList, random, 0, i);
	
	                if (structurecomponent1 != null)
	                {
	                	StructureBoundingBox b = (StructureBoundingBox) VillagePieces.boundingBox.get(structurecomponent1);
	                    i += Math.max(b.getXSize(), b.getZSize());
	                    flag = true;
	                }
	            }
            }
            catch(final Exception t) {
            	;
            }
            
            if (flag && random.nextInt(3) > 0)
            {
                switch (this.coordBaseMode)
                {
                    case 0:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, 1, this.getComponentType());
                        break;
                    case 1:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, this.getComponentType());
                        break;
                    case 2:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, 1, this.getComponentType());
                        break;
                    case 3:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, this.getComponentType());
                }
            }

            if (flag && random.nextInt(3) > 0)
            {
                switch (this.coordBaseMode)
                {
                    case 0:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, 3, this.getComponentType());
                        break;
                    case 1:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, this.getComponentType());
                        break;
                    case 2:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, 3, this.getComponentType());
                        break;
                    case 3:
                        getNextComponentVillagePath((MyStart)start, componentList, random, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, this.getComponentType());
                }
            }
        }


        // This plots out the roadbed
        @Override
        public boolean addComponentParts(World world, Random random, StructureBoundingBox box)
        {
            Block block = this.func_151558_b(Blocks.cobblestone, 0);

            for (int i = this.boundingBox.minX; i <= this.boundingBox.maxX; ++i)
            {
                for (int j = this.boundingBox.minZ; j <= this.boundingBox.maxZ; ++j)
                {
                    if (box.isVecInside(i, 64, j))
                    {
                        int k = world.getTopSolidOrLiquidBlock(i, j) - 1;
                        if ( k < world.provider.getAverageGroundLevel())
                        	k = world.provider.getAverageGroundLevel() - 1;
                        world.setBlock(i, k, j, block, 0, 2);
                    }
                }
            }

            return true;
        }
        
        @SuppressWarnings({ "rawtypes", "unchecked" })
		protected StructureComponent getNextComponentNN(StructureVillagePieces.Start start, List componentList, Random random, int x, int z)
        {
            switch (this.coordBaseMode)
            {
                case 0:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.minX - 1, this.boundingBox.minY + x, this.boundingBox.minZ + z, 1, this.getComponentType());
                case 1:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.minX + z, this.boundingBox.minY + x, this.boundingBox.minZ - 1, 2, this.getComponentType());
                case 2:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.minX - 1, this.boundingBox.minY + x, this.boundingBox.minZ + z, 1, this.getComponentType());
                case 3:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.minX + z, this.boundingBox.minY + x, this.boundingBox.minZ - 1, 2, this.getComponentType());
                default:
                    return null;
            }
        }

        /**
         * Gets the next village component, with the bounding box shifted +1 in the X and Z direction.
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
		protected StructureComponent getNextComponentPP(StructureVillagePieces.Start start, List componentList, Random random, int x, int z)
        {
            switch (this.coordBaseMode)
            {
                case 0:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.maxX + 1, this.boundingBox.minY + x, this.boundingBox.minZ + z, 3, this.getComponentType());
                case 1:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.minX + z, this.boundingBox.minY + x, this.boundingBox.maxZ + 1, 0, this.getComponentType());
                case 2:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.maxX + 1, this.boundingBox.minY + x, this.boundingBox.minZ + z, 3, this.getComponentType());
                case 3:
                    return getNextVillageStructureComponent(start, componentList, random, this.boundingBox.minX + z, this.boundingBox.minY + x, this.boundingBox.maxZ + 1, 0, this.getComponentType());
                default:
                    return null;
            }
        }

        
        public StructureBoundingBox getBoundingBox() {
        	return this.boundingBox;
        }
	}
}
