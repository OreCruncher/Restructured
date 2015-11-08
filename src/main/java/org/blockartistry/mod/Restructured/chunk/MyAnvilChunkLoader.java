/* This file is part of Restructured, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.Restructured.chunk;

import cpw.mods.fml.common.FMLLog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of a new AnvilChunkLoader. The improvements that have been
 * made:
 * 
 * + Single list tracking pending write operations rather than having two lists
 * which resulted in a double search.
 * 
 * + Use a Map instead of array for lookups of cached writes. Eliminate
 * PendingChunk in the process.
 * 
 * + Issue a close() on the ChunkInputStream object when the chunk has been
 * deserialized. This permits the object to be placed back into the object pool.
 * 
 * + Refine the scope of locks that need to be held so that they are as narrow
 * as possible.
 * 
 * + Removed the session lock check during chunk save. The check was opening and
 * reading the lock file every time a chunk save came through. Mechanism should
 * be changed to use the file as a semaphore by obtaining an exclusive lock when
 * the world loads and croak at that time if there is contention.
 *
 * Note that this implementation uses a Map rather than ArrayList for tracking
 * the pending writes. At the moment having an entry in the list for save is
 * more important than any other particular FIFO need.  In fact the hashCode
 * may provide a degree of randomization when it comes to write and avoid
 * the serial nature of writing chunks since the underlying RegionMap are
 * synchronized.
 */
public class MyAnvilChunkLoader implements IChunkLoader, IThreadedFileIO {

	private static final Logger logger = LogManager.getLogger();
	public final File chunkSaveLocation;
	private final Map<ChunkCoordIntPair, NBTTagCompound> pendingIO = new HashMap<ChunkCoordIntPair, NBTTagCompound>();

	public MyAnvilChunkLoader(final File saveLocation) {
		this.chunkSaveLocation = saveLocation;
	}

	public boolean chunkExists(final World world, final int chunkX, final int chunkZ) {

		final ChunkCoordIntPair coords = new ChunkCoordIntPair(chunkX, chunkZ);
		synchronized (pendingIO) {
			if (pendingIO.containsKey(coords))
				return true;
		}

		return MyRegionCache.createOrLoadRegionFile(chunkSaveLocation, chunkX, chunkZ).chunkExists(chunkX & 31,
				chunkZ & 31);
	}

	public Chunk loadChunk(final World world, final int chunkX, final int chunkZ) throws IOException {
		final Object[] data = loadChunk__Async(world, chunkX, chunkZ);
		if (data != null) {
			final Chunk chunk = (Chunk) data[0];
			final NBTTagCompound nbt = (NBTTagCompound) data[1];
			loadEntities(world, nbt.getCompoundTag("Level"), chunk);
			return chunk;
		}
		return null;
	}

	public Object[] loadChunk__Async(final World world, final int chunkX, final int chunkZ) throws IOException {
		NBTTagCompound nbt = null;
		final ChunkCoordIntPair coords = new ChunkCoordIntPair(chunkX, chunkZ);
		synchronized (pendingIO) {
			nbt = pendingIO.get(coords);
		}

		if (nbt == null) {
			final DataInputStream stream = MyRegionCache.getChunkInputStream(chunkSaveLocation, chunkX, chunkZ);
			if (stream == null) {
				return null;
			}
			nbt = CompressedStreamTools.read(stream);

			// Need this. Underneath it triggers the routines to put
			// the stream into it's object pool for reuse. Besides,
			// you should always close a stream when done because it
			// may be needed.
			stream.close();
		}

		return checkedReadChunkFromNBT__Async(world, chunkX, chunkZ, nbt);
	}

	protected Chunk checkedReadChunkFromNBT(final World world, final int chunkX, final int chunkZ, NBTTagCompound nbt) {
		final Object[] data = checkedReadChunkFromNBT__Async(world, chunkX, chunkZ, nbt);
		return data != null ? (Chunk) data[0] : null;
	}

	protected Object[] checkedReadChunkFromNBT__Async(final World world, final int chunkX, final int chunkZ,
			final NBTTagCompound nbt) {
		if (!nbt.hasKey("Level", 10)) {
			logger.error("Chunk file at " + chunkX + "," + chunkZ + " is missing level data, skipping");
			return null;
		}
		if (!nbt.getCompoundTag("Level").hasKey("Sections", 9)) {
			logger.error("Chunk file at " + chunkX + "," + chunkZ + " is missing block data, skipping");
			return null;
		}
		Chunk chunk = readChunkFromNBT(world, nbt.getCompoundTag("Level"));
		if (!chunk.isAtLocation(chunkX, chunkZ)) {
			logger.error("Chunk file at " + chunkX + "," + chunkZ + " is in the wrong location; relocating. (Expected "
					+ chunkX + ", " + chunkZ + ", got " + chunk.xPosition + ", " + chunk.zPosition + ")");
			nbt.setInteger("xPos", chunkX);
			nbt.setInteger("zPos", chunkZ);

			NBTTagList tileEntities = nbt.getCompoundTag("Level").getTagList("TileEntities", 10);
			if (tileEntities != null) {
				for (int te = 0; te < tileEntities.tagCount(); te++) {
					NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(te);
					int x = tileEntity.getInteger("x") - chunk.xPosition * 16;
					int z = tileEntity.getInteger("z") - chunk.zPosition * 16;
					tileEntity.setInteger("x", chunkX * 16 + x);
					tileEntity.setInteger("z", chunkZ * 16 + z);
				}
			}
			chunk = readChunkFromNBT(world, nbt.getCompoundTag("Level"));
		}

		return new Object[] { chunk, nbt };
	}

	public void saveChunk(final World world, final Chunk chunk) throws MinecraftException, IOException {
		try {

			final NBTTagCompound nbt1 = new NBTTagCompound();
			final NBTTagCompound nbt2 = new NBTTagCompound();
			nbt1.setTag("Level", nbt2);
			writeChunkToNBT(chunk, world, nbt2);
			MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, nbt1));

			final ChunkCoordIntPair coords = chunk.getChunkCoordIntPair();

			synchronized (pendingIO) {
				pendingIO.put(coords, nbt1);
			}

			MyThreadedFileIOBase.threadedIOInstance.queueIO(this);

		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	public boolean writeNextIO() {
		ChunkCoordIntPair coords = null;
		NBTTagCompound nbt = null;

		synchronized (pendingIO) {
			if (!pendingIO.isEmpty()) {
				// Peel the first entry from the map
				coords = pendingIO.keySet().iterator().next();
				nbt = pendingIO.remove(coords);
			}
		}

		if (nbt != null) {
			try {
				writeChunkNBTTags(coords, nbt);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return true;
	}

	private void writeChunkNBTTags(final ChunkCoordIntPair coords, final NBTTagCompound nbt) throws IOException {
		final DataOutputStream stream = MyRegionCache.getChunkOutputStream(chunkSaveLocation, coords.chunkXPos,
				coords.chunkZPos);
		CompressedStreamTools.write(nbt, stream);
		stream.close();
	}

	public void saveExtraChunkData(final World world, final Chunk chunk) {
	}

	public void chunkTick() {
	}

	public void saveExtraData() {
		while (writeNextIO()) {
		}
	}

	private void writeChunkToNBT(final Chunk chunk, final World world, final NBTTagCompound nbt) {
		nbt.setByte("V", (byte) 1);
		nbt.setInteger("xPos", chunk.xPosition);
		nbt.setInteger("zPos", chunk.zPosition);
		nbt.setLong("LastUpdate", world.getTotalWorldTime());
		nbt.setIntArray("HeightMap", chunk.heightMap);
		nbt.setBoolean("TerrainPopulated", chunk.isTerrainPopulated);
		nbt.setBoolean("LightPopulated", chunk.isLightPopulated);
		nbt.setLong("InhabitedTime", chunk.inhabitedTime);
		ExtendedBlockStorage[] aextendedblockstorage = chunk.getBlockStorageArray();
		NBTTagList nbttaglist = new NBTTagList();
		boolean flag = !world.provider.hasNoSky;
		ExtendedBlockStorage[] aextendedblockstorage1 = aextendedblockstorage;
		int i = aextendedblockstorage.length;
		for (int j = 0; j < i; j++) {
			ExtendedBlockStorage extendedblockstorage = aextendedblockstorage1[j];
			if (extendedblockstorage != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Y", (byte) (extendedblockstorage.getYLocation() >> 4 & 0xFF));
				nbttagcompound1.setByteArray("Blocks", extendedblockstorage.getBlockLSBArray());
				if (extendedblockstorage.getBlockMSBArray() != null) {
					nbttagcompound1.setByteArray("Add", extendedblockstorage.getBlockMSBArray().data);
				}
				nbttagcompound1.setByteArray("Data", extendedblockstorage.getMetadataArray().data);
				nbttagcompound1.setByteArray("BlockLight", extendedblockstorage.getBlocklightArray().data);
				if (flag) {
					nbttagcompound1.setByteArray("SkyLight", extendedblockstorage.getSkylightArray().data);
				} else {
					nbttagcompound1.setByteArray("SkyLight",
							new byte[extendedblockstorage.getBlocklightArray().data.length]);
				}
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		nbt.setTag("Sections", nbttaglist);
		nbt.setByteArray("Biomes", chunk.getBiomeArray());
		chunk.hasEntities = false;
		NBTTagList nbttaglist2 = new NBTTagList();
		for (i = 0; i < chunk.entityLists.length; i++) {
			Iterator<?> iterator1 = chunk.entityLists[i].iterator();
			while (iterator1.hasNext()) {
				Entity entity = (Entity) iterator1.next();
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				try {
					if (entity.writeToNBTOptional(nbttagcompound1)) {
						chunk.hasEntities = true;
						nbttaglist2.appendTag(nbttagcompound1);
					}
				} catch (Exception e) {
					FMLLog.log(Level.ERROR, e,
							"An Entity type %s has thrown an exception trying to write state. It will not persist. Report this to the mod author",
							new Object[] { entity.getClass().getName() });
				}
			}
		}
		nbt.setTag("Entities", nbttaglist2);
		NBTTagList nbttaglist3 = new NBTTagList();
		Iterator<?> iterator1 = chunk.chunkTileEntityMap.values().iterator();
		while (iterator1.hasNext()) {
			TileEntity tileentity = (TileEntity) iterator1.next();
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			try {
				tileentity.writeToNBT(nbttagcompound1);
				nbttaglist3.appendTag(nbttagcompound1);
			} catch (Exception e) {
				FMLLog.log(Level.ERROR, e,
						"A TileEntity type %s has throw an exception trying to write state. It will not persist. Report this to the mod author",
						new Object[] { tileentity.getClass().getName() });
			}
		}
		nbt.setTag("TileEntities", nbttaglist3);
		List<?> list = world.getPendingBlockUpdates(chunk, false);
		if (list != null) {
			long k = world.getTotalWorldTime();
			NBTTagList nbttaglist1 = new NBTTagList();
			Iterator<?> iterator = list.iterator();
			while (iterator.hasNext()) {
				NextTickListEntry nextticklistentry = (NextTickListEntry) iterator.next();
				NBTTagCompound nbttagcompound2 = new NBTTagCompound();
				nbttagcompound2.setInteger("i", Block.getIdFromBlock(nextticklistentry.func_151351_a()));
				nbttagcompound2.setInteger("x", nextticklistentry.xCoord);
				nbttagcompound2.setInteger("y", nextticklistentry.yCoord);
				nbttagcompound2.setInteger("z", nextticklistentry.zCoord);
				nbttagcompound2.setInteger("t", (int) (nextticklistentry.scheduledTime - k));
				nbttagcompound2.setInteger("p", nextticklistentry.priority);
				nbttaglist1.appendTag(nbttagcompound2);
			}
			nbt.setTag("TileTicks", nbttaglist1);
		}
	}

	private Chunk readChunkFromNBT(final World world, final NBTTagCompound nbt) {
		int i = nbt.getInteger("xPos");
		int j = nbt.getInteger("zPos");
		Chunk chunk = new Chunk(world, i, j);
		chunk.heightMap = nbt.getIntArray("HeightMap");
		chunk.isTerrainPopulated = nbt.getBoolean("TerrainPopulated");
		chunk.isLightPopulated = nbt.getBoolean("LightPopulated");
		chunk.inhabitedTime = nbt.getLong("InhabitedTime");
		NBTTagList nbttaglist = nbt.getTagList("Sections", 10);
		byte b0 = 16;
		ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[b0];
		boolean flag = !world.provider.hasNoSky;
		for (int k = 0; k < nbttaglist.tagCount(); k++) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(k);
			byte b1 = nbttagcompound1.getByte("Y");
			ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(b1 << 4, flag);
			extendedblockstorage.setBlockLSBArray(nbttagcompound1.getByteArray("Blocks"));
			if (nbttagcompound1.hasKey("Add", 7)) {
				extendedblockstorage.setBlockMSBArray(new NibbleArray(nbttagcompound1.getByteArray("Add"), 4));
			}
			extendedblockstorage.setBlockMetadataArray(new NibbleArray(nbttagcompound1.getByteArray("Data"), 4));
			extendedblockstorage.setBlocklightArray(new NibbleArray(nbttagcompound1.getByteArray("BlockLight"), 4));
			if (flag) {
				extendedblockstorage.setSkylightArray(new NibbleArray(nbttagcompound1.getByteArray("SkyLight"), 4));
			}
			extendedblockstorage.removeInvalidBlocks();
			aextendedblockstorage[b1] = extendedblockstorage;
		}
		chunk.setStorageArrays(aextendedblockstorage);
		if (nbt.hasKey("Biomes", 7)) {
			chunk.setBiomeArray(nbt.getByteArray("Biomes"));
		}
		return chunk;
	}

	public void loadEntities(final World world, final NBTTagCompound nbt, final Chunk chunk) {
		NBTTagList nbttaglist1 = nbt.getTagList("Entities", 10);
		if (nbttaglist1 != null) {
			for (int l = 0; l < nbttaglist1.tagCount(); l++) {
				NBTTagCompound nbttagcompound3 = nbttaglist1.getCompoundTagAt(l);
				Entity entity2 = EntityList.createEntityFromNBT(nbttagcompound3, world);
				chunk.hasEntities = true;
				if (entity2 != null) {
					chunk.addEntity(entity2);
					Entity entity = entity2;
					for (NBTTagCompound nbttagcompound2 = nbttagcompound3; nbttagcompound2.hasKey("Riding",
							10); nbttagcompound2 = nbttagcompound2.getCompoundTag("Riding")) {
						Entity entity1 = EntityList.createEntityFromNBT(nbttagcompound2.getCompoundTag("Riding"),
								world);
						if (entity1 != null) {
							chunk.addEntity(entity1);
							entity.mountEntity(entity1);
						}
						entity = entity1;
					}
				}
			}
		}
		NBTTagList nbttaglist2 = nbt.getTagList("TileEntities", 10);
		if (nbttaglist2 != null) {
			for (int i1 = 0; i1 < nbttaglist2.tagCount(); i1++) {
				NBTTagCompound nbttagcompound4 = nbttaglist2.getCompoundTagAt(i1);
				TileEntity tileentity = TileEntity.createAndLoadEntity(nbttagcompound4);
				if (tileentity != null) {
					chunk.addTileEntity(tileentity);
				}
			}
		}
		if (nbt.hasKey("TileTicks", 9)) {
			NBTTagList nbttaglist3 = nbt.getTagList("TileTicks", 10);
			if (nbttaglist3 != null) {
				for (int j1 = 0; j1 < nbttaglist3.tagCount(); j1++) {
					NBTTagCompound nbttagcompound5 = nbttaglist3.getCompoundTagAt(j1);
					world.func_147446_b(nbttagcompound5.getInteger("x"), nbttagcompound5.getInteger("y"),
							nbttagcompound5.getInteger("z"), Block.getBlockById(nbttagcompound5.getInteger("i")),
							nbttagcompound5.getInteger("t"), nbttagcompound5.getInteger("p"));
				}
			}
		}
	}
}
