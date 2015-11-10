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
 * more important than any other particular FIFO need. In fact the hashCode may
 * provide a degree of randomization when it comes to write and avoid the serial
 * nature of writing chunks since the underlying RegionMap are synchronized.
 */
public class MyAnvilChunkLoader implements IChunkLoader, IThreadedFileIO {

	private static final Logger logger = LogManager.getLogger();
	public final File chunkSaveLocation;

	// To explain there is some hackish type things going on. With multiple
	// threads running IO there is a possibility that multiple threads could
	// attempt writing the same chunk but with different data versions. For
	// example:
	//
	// 1. Data for Chunk XZ could be pulled off by the IO Thread 1. Context
	// switch occurs.
	//
	// 2. saveChunk() queues up another update for Chunk XZ. Context switch
	// occurs.
	//
	// 3. IO Thread 2 pulls off the 2nd Chunk XZ data and starts the write.
	// It gets a lock on the underlying RegionFile. Context switch.
	//
	// 4. IO Thread 1 starts to write, but blocks on the underlying RegionFile
	// synchronized method lock. Context switch.
	//
	// 5. IO Thread 2 completes the write. New data written. Context switch.
	//
	// 6. IO Thread 1 completes the write. Old data overwrites new data.
	//
	// The LockManager<> is intended to mitigate this scenario. The IO Thread
	// will obtain a lock with the manager based on the chunk coordinates.
	// If there isn't a pending lock for that coordinate one is established
	// and the IO Thread continues. If a lock is already being held it will
	// block until the lock is released by the other IO Thread.
	//
	// Note that this chunk coordinate lock is obtained while the lock on
	// pendingIO is held. If a thread were to block on both locks all save
	// operations will block until the thread that owns both locks completes
	// it's activity. Once that happens concurrent behavior will continue
	// until such a time a similar circumstance arises.
	//
	// Note that the chances of this actually happening are pretty slim.
	// However, it is possible so it has to be guarded against. Ideally
	// the whole Chunk IO cache/write stack should be refactored around
	// concurrent behavior, but since I am attempting to slide these changes
	// in under the hood there is a limit to what can be accomplished.
	//
	// It's starting to feel like I am back at my old job...
	//
	private final LockManager<ChunkCoordIntPair> locks = new LockManager<ChunkCoordIntPair>();
	private final Map<ChunkCoordIntPair, NBTTagCompound> pendingIO = new HashMap<ChunkCoordIntPair, NBTTagCompound>();

	public MyAnvilChunkLoader(final File saveLocation) {
		this.chunkSaveLocation = saveLocation;
	}

	private static byte[] duplicate(final byte[] src) {
		final byte[] newArray = new byte[src.length];
		System.arraycopy(src, 0, newArray, 0, src.length);
		return newArray;
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

			final NBTTagList tileEntities = nbt.getCompoundTag("Level").getTagList("TileEntities", 10);
			if (tileEntities != null) {
				for (int te = 0; te < tileEntities.tagCount(); te++) {
					final NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(te);
					final int x = tileEntity.getInteger("x") - chunk.xPosition * 16;
					final int z = tileEntity.getInteger("z") - chunk.zPosition * 16;
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
			if (pendingIO.isEmpty())
				return false;

			// Peel the first entry from the map
			coords = pendingIO.keySet().iterator().next();
			nbt = pendingIO.remove(coords);

			// If this lock attempt blocks the lock on
			// pendingIO will be held until the lock
			// clears. The only time the lock should
			// block is if another IO thread is currently
			// handling the same chunk.
			if (nbt != null)
				try {
					locks.lock(coords);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
		}

		if (nbt != null) {
			try {
				writeChunkNBTTags(coords, nbt);
			} catch (final Exception exception) {
				exception.printStackTrace();
			} finally {
				// Make sure the lock is cleared
				locks.unlock(coords);
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

	@SuppressWarnings("unchecked")
	private void writeChunkToNBT(final Chunk chunk, final World world, final NBTTagCompound nbt) {
		nbt.setByte("V", (byte) 1);
		nbt.setInteger("xPos", chunk.xPosition);
		nbt.setInteger("zPos", chunk.zPosition);
		nbt.setLong("LastUpdate", world.getTotalWorldTime());
		nbt.setIntArray("HeightMap", chunk.heightMap);
		nbt.setBoolean("TerrainPopulated", chunk.isTerrainPopulated);
		nbt.setBoolean("LightPopulated", chunk.isLightPopulated);
		nbt.setLong("InhabitedTime", chunk.inhabitedTime);

		NBTTagCompound scratch = null;

		final NBTTagList sections = new NBTTagList();
		final boolean flag = !world.provider.hasNoSky;
		final ExtendedBlockStorage[] ebs = chunk.getBlockStorageArray();
		for (int j = 0; j < ebs.length; j++) {
			final ExtendedBlockStorage tebs = ebs[j];
			if (tebs != null) {
				scratch = new NBTTagCompound();
				scratch.setByte("Y", (byte) (tebs.getYLocation() >> 4 & 0xFF));
				scratch.setByteArray("Blocks", duplicate(tebs.getBlockLSBArray()));
				if (tebs.getBlockMSBArray() != null) {
					scratch.setByteArray("Add", duplicate(tebs.getBlockMSBArray().data));
				}
				scratch.setByteArray("Data", duplicate(tebs.getMetadataArray().data));
				scratch.setByteArray("BlockLight", duplicate(tebs.getBlocklightArray().data));
				if (flag) {
					scratch.setByteArray("SkyLight", duplicate(tebs.getSkylightArray().data));
				} else {
					scratch.setByteArray("SkyLight", new byte[tebs.getBlocklightArray().data.length]);
				}
				sections.appendTag(scratch);
			}
		}
		nbt.setTag("Sections", sections);

		nbt.setByteArray("Biomes", duplicate(chunk.getBiomeArray()));

		final NBTTagList entities = new NBTTagList();
		for (int i = 0; i < chunk.entityLists.length; i++) {
			for (final Entity entity : (List<Entity>) chunk.entityLists[i]) {
				scratch = new NBTTagCompound();
				try {
					if (entity.writeToNBTOptional(scratch)) {
						entities.appendTag(scratch);
					}
				} catch (final Exception e) {
					FMLLog.log(Level.ERROR, e,
							"An Entity type %s has thrown an exception trying to write state. It will not persist. Report this to the mod author",
							new Object[] { entity.getClass().getName() });
				}
			}
		}
		chunk.hasEntities = entities.tagCount() > 0;
		nbt.setTag("Entities", entities);

		final NBTTagList tileEntities = new NBTTagList();
		for (final TileEntity tileentity : (Iterable<TileEntity>) chunk.chunkTileEntityMap.values()) {
			scratch = new NBTTagCompound();
			try {
				tileentity.writeToNBT(scratch);
				tileEntities.appendTag(scratch);
			} catch (final Exception e) {
				FMLLog.log(Level.ERROR, e,
						"A TileEntity type %s has throw an exception trying to write state. It will not persist. Report this to the mod author",
						new Object[] { tileentity.getClass().getName() });
			}
		}
		nbt.setTag("TileEntities", tileEntities);

		final List<NextTickListEntry> list = world.getPendingBlockUpdates(chunk, false);
		if (list != null) {
			final long k = world.getTotalWorldTime();
			final NBTTagList tileTicks = new NBTTagList();
			for (final NextTickListEntry tickEntry : list) {
				scratch = new NBTTagCompound();
				scratch.setInteger("i", Block.getIdFromBlock(tickEntry.func_151351_a()));
				scratch.setInteger("x", tickEntry.xCoord);
				scratch.setInteger("y", tickEntry.yCoord);
				scratch.setInteger("z", tickEntry.zCoord);
				scratch.setInteger("t", (int) (tickEntry.scheduledTime - k));
				scratch.setInteger("p", tickEntry.priority);
				tileTicks.appendTag(scratch);
			}
			nbt.setTag("TileTicks", tileTicks);
		}
	}

	private Chunk readChunkFromNBT(final World world, final NBTTagCompound nbt) {
		final int i = nbt.getInteger("xPos");
		final int j = nbt.getInteger("zPos");
		final Chunk chunk = new Chunk(world, i, j);
		chunk.heightMap = nbt.getIntArray("HeightMap");
		chunk.isTerrainPopulated = nbt.getBoolean("TerrainPopulated");
		chunk.isLightPopulated = nbt.getBoolean("LightPopulated");
		chunk.inhabitedTime = nbt.getLong("InhabitedTime");
		final NBTTagList sections = nbt.getTagList("Sections", 10);
		final ExtendedBlockStorage[] ebs = new ExtendedBlockStorage[16];
		final boolean flag = !world.provider.hasNoSky;
		for (int k = 0; k < sections.tagCount(); k++) {
			final NBTTagCompound scratch = sections.getCompoundTagAt(k);
			final byte b1 = scratch.getByte("Y");
			final ExtendedBlockStorage tebs = new ExtendedBlockStorage(b1 << 4, flag);
			tebs.setBlockLSBArray(duplicate(scratch.getByteArray("Blocks")));
			if (scratch.hasKey("Add", 7)) {
				tebs.setBlockMSBArray(new NibbleArray(duplicate(scratch.getByteArray("Add")), 4));
			}
			tebs.setBlockMetadataArray(new NibbleArray(duplicate(scratch.getByteArray("Data")), 4));
			tebs.setBlocklightArray(new NibbleArray(duplicate(scratch.getByteArray("BlockLight")), 4));
			if (flag) {
				tebs.setSkylightArray(new NibbleArray(duplicate(scratch.getByteArray("SkyLight")), 4));
			}
			tebs.removeInvalidBlocks();
			ebs[b1] = tebs;
		}
		chunk.setStorageArrays(ebs);
		if (nbt.hasKey("Biomes", 7)) {
			chunk.setBiomeArray(duplicate(nbt.getByteArray("Biomes")));
		}
		return chunk;
	}

	public void loadEntities(final World world, final NBTTagCompound nbt, final Chunk chunk) {
		NBTTagCompound scratch = null;
		final NBTTagList entities = nbt.getTagList("Entities", 10);
		if (entities != null) {
			for (int l = 0; l < entities.tagCount(); l++) {
				scratch = entities.getCompoundTagAt(l);
				final Entity entity2 = EntityList.createEntityFromNBT(scratch, world);
				chunk.hasEntities = true;
				if (entity2 != null) {
					chunk.addEntity(entity2);
					Entity entity = entity2;
					for (NBTTagCompound nbttagcompound2 = scratch; nbttagcompound2.hasKey("Riding",
							10); nbttagcompound2 = nbttagcompound2.getCompoundTag("Riding")) {
						final Entity entity1 = EntityList.createEntityFromNBT(nbttagcompound2.getCompoundTag("Riding"),
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

		final NBTTagList tileEntities = nbt.getTagList("TileEntities", 10);
		if (tileEntities != null) {
			for (int i1 = 0; i1 < tileEntities.tagCount(); i1++) {
				scratch = tileEntities.getCompoundTagAt(i1);
				final TileEntity tileentity = TileEntity.createAndLoadEntity(scratch);
				if (tileentity != null) {
					chunk.addTileEntity(tileentity);
				}
			}
		}

		if (nbt.hasKey("TileTicks", 9)) {
			final NBTTagList tileTicks = nbt.getTagList("TileTicks", 10);
			if (tileTicks != null) {
				for (int j1 = 0; j1 < tileTicks.tagCount(); j1++) {
					scratch = tileTicks.getCompoundTagAt(j1);
					world.func_147446_b(scratch.getInteger("x"), scratch.getInteger("y"), scratch.getInteger("z"),
							Block.getBlockById(scratch.getInteger("i")), scratch.getInteger("t"),
							scratch.getInteger("p"));
				}
			}
		}
	}
}
