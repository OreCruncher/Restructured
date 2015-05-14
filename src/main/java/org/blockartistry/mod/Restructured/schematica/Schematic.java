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

/*
 * The majority of the source in this file was obtained from LatBlocks, an MIT
 * licensed open source project managed by Lunatrius.  You can find that project
 * at: https://github.com/Lunatrius/Schematica
 * 
 * This source file has been modified from the original to suit the purpose of
 * this project.  If you are looking to reuse this code it is heavily suggested
 * that the source be acquired from the LatBlocks source vault.
 */

package org.blockartistry.mod.Restructured.schematica;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Schematic implements ISchematic {
    private static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.grass);
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();

    private ItemStack icon;
    private final short[][][] blocks;
    private final byte[][][] metadata;
    private final List<TileEntity> tileEntities = new ArrayList<TileEntity>();
    private final List<Entity> entities = new ArrayList<Entity>();
    private final int width;
    private final int height;
    private final int length;

    public Schematic(final ItemStack icon, final int width, final int height, final int length) {
        this.icon = icon;
        this.blocks = new short[width][height][length];
        this.metadata = new byte[width][height][length];

        this.width = width;
        this.height = height;
        this.length = length;
    }

    @Override
    public Block getBlock(final int x, final int y, final int z) {
        if (!isValid(x, y, z)) {
            return Blocks.air;
        }

        return BLOCK_REGISTRY.getObjectById(this.blocks[x][y][z]);
    }

    public boolean setBlock(final int x, final int y, final int z, final Block block) {
        return setBlock(x, y, z, block, 0);
    }

    public boolean setBlock(final int x, final int y, final int z, final Block block, final int metadata) {
        if (!isValid(x, y, z)) {
            return false;
        }

        final int id = BLOCK_REGISTRY.getId(block);
        if (id == -1) {
            return false;
        }

        this.blocks[x][y][z] = (short) id;
        setBlockMetadata(x, y, z, metadata);
        return true;
    }

    public TileEntity getTileEntity(final int x, final int y, final int z) {
        for (final TileEntity tileEntity : this.tileEntities) {
            if (tileEntity.xCoord == x && tileEntity.yCoord == y && tileEntity.zCoord == z) {
                return tileEntity;
            }
        }

        return null;
    }

    @Override
    public List<TileEntity> getTileEntities() {
        return this.tileEntities;
    }

    public void setTileEntity(final int x, final int y, final int z, final TileEntity tileEntity) {
        if (!isValid(x, y, z)) {
            return;
        }

        this.removeTileEntity(x, y, z);

        if (tileEntity != null) {
            this.tileEntities.add(tileEntity);
        }
    }

    public void removeTileEntity(final int x, final int y, final int z) {
        final Iterator<TileEntity> iterator = this.tileEntities.iterator();

        while (iterator.hasNext()) {
            final TileEntity tileEntity = iterator.next();
            if (tileEntity.xCoord == x && tileEntity.yCoord == y && tileEntity.zCoord == z) {
                iterator.remove();
            }
        }
    }

    @Override
    public int getBlockMetadata(final int x, final int y, final int z) {
        if (!isValid(x, y, z)) {
            return 0;
        }

        return this.metadata[x][y][z];
    }

    public boolean setBlockMetadata(final int x, final int y, final int z, final int metadata) {
        if (!isValid(x, y, z)) {
            return false;
        }

        this.metadata[x][y][z] = (byte) (metadata & 0x0F);
        return true;
    }

    @Override
    public List<Entity> getEntities() {
        return this.entities;
    }

    public void addEntity(final Entity entity) {
        if (entity == null || entity.getUniqueID() == null || entity instanceof EntityPlayer) {
            return;
        }

        for (final Entity e : this.entities) {
            if (entity.getUniqueID().equals(e.getUniqueID())) {
                return;
            }
        }

        this.entities.add(entity);
    }

    public void removeEntity(final Entity entity) {
        if (entity == null || entity.getUniqueID() == null) {
            return;
        }

        final Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext()) {
            final Entity e = iterator.next();
            if (entity.getUniqueID().equals(e.getUniqueID())) {
                iterator.remove();
            }
        }
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public void setIcon(final ItemStack icon) {
        if (icon != null) {
            this.icon = icon;
        } else {
            this.icon = DEFAULT_ICON.copy();
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    private boolean isValid(final int x, final int y, final int z) {
        return !(x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
    }
}