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

package org.blockartistry.mod.Restructured.asm;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.CheckClassAdapter;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer {

	private static final boolean DO_NBT = true;

	private static Map<String, String> targets = new HashMap<String, String>();
	private static Map<String, String> typesToReplace = new HashMap<String, String>();
	private static Map<String, String> obsRemap = new HashMap<String, String>();

	static {

		targets.put("net.minecraft.world.chunk.storage.RegionFileCache", "chunk/MyRegionCache");
		targets.put("net.minecraft.world.chunk.storage.RegionFile", "chunk/MyRegionFile");
		targets.put("net.minecraft.world.chunk.storage.ChunkBuffer", "chunk/MyChunkBuffer");
		targets.put("net.minecraft.world.chunk.storage.RegionFileLRU", "chunk/RegionFileLRU");
		targets.put("net.minecraft.world.chunk.storage.LockManager", "chunk/LockManager");
		targets.put("net.minecraft.world.storage.ThreadedFileIOBase", "chunk/MyThreadedFileIOBase");
		targets.put("net.minecraft.world.storage.ChunkOutputStream", "chunk/ChunkOutputStream");
		targets.put("net.minecraft.world.storage.ChunkInputStream", "chunk/ChunkInputStream");
		targets.put("net.minecraft.world.chunk.storage.AnvilChunkLoader", "chunk/MyAnvilChunkLoader");

		targets.put("aqj", "chunk/MyRegionCache");
		targets.put("aqh", "chunk/MyRegionFile");
		targets.put("azr", "chunk/MyThreadedFileIOBase");
		targets.put("aqk", "chunk/MyAnvilChunkLoader");

		if (DO_NBT) {
			targets.put("net.minecraft.nbt.NBTSizeTracker$NoopNBTTracker", "nbt/MyNBTSizeTracker$NoopNBTTracker");
			targets.put("net.minecraft.nbt.CompressedStreamTools", "nbt/MyCompressedStreamTools");
			targets.put("net.minecraft.nbt.NBTFactory", "nbt/NBTFactory");
			targets.put("net.minecraft.nbt.NBTBase", "nbt/MyNBTBase");
			targets.put("net.minecraft.nbt.NBTPrimitive", "nbt/MyNBTPrimitive");
			targets.put("net.minecraft.nbt.NBTSizeTracker", "nbt/MyNBTSizeTracker");
			targets.put("net.minecraft.nbt.NBTTagByte", "nbt/MyNBTTagByte");
			targets.put("net.minecraft.nbt.NBTTagByteArray", "nbt/MyNBTTagByteArray");
			targets.put("net.minecraft.nbt.NBTTagCompound", "nbt/MyNBTTagCompound");
			targets.put("net.minecraft.nbt.NBTTagDouble", "nbt/MyNBTTagDouble");
			targets.put("net.minecraft.nbt.NBTTagEnd", "nbt/MyNBTTagEnd");
			targets.put("net.minecraft.nbt.NBTTagFloat", "nbt/MyNBTTagFloat");
			targets.put("net.minecraft.nbt.NBTTagInt", "nbt/MyNBTTagInt");
			targets.put("net.minecraft.nbt.NBTTagIntArray", "nbt/MyNBTTagIntArray");
			targets.put("net.minecraft.nbt.NBTTagList", "nbt/MyNBTTagList");
			targets.put("net.minecraft.nbt.NBTTagLong", "nbt/MyNBTTagLong");
			targets.put("net.minecraft.nbt.NBTTagShort", "nbt/MyNBTTagShort");
			targets.put("net.minecraft.nbt.NBTTagString", "nbt/MyNBTTagString");
		}

		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.MyAnvilChunkLoader",
				"net.minecraft.world.chunk.storage.AnvilChunkLoader");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.MyRegionCache",
				"net.minecraft.world.chunk.storage.RegionFileCache");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.MyRegionFile",
				"net.minecraft.world.chunk.storage.RegionFile");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.MyChunkBuffer",
				"net.minecraft.world.chunk.storage.ChunkBuffer");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.RegionFileLRU",
				"net.minecraft.world.chunk.storage.RegionFileLRU");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.LockManager",
				"net.minecraft.world.chunk.storage.LockManager");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.MyThreadedFileIOBase",
				"net.minecraft.world.storage.ThreadedFileIOBase");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.ChunkOutputStream",
				"net.minecraft.world.storage.ChunkOutputStream");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.ChunkInputStream",
				"net.minecraft.world.storage.ChunkInputStream");

		if (DO_NBT) {

			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyCompressedStreamTools", "net.minecraft.nbt.CompressedStreamTools");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.NBTBaseFactory", "net.minecraft.nbt.NBTFactory");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTBase", "net.minecraft.nbt.NBTBase");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTPrimitive",
					"net.minecraft.nbt.NBTPrimitive");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTSizeTracker",
					"net.minecraft.nbt.NBTSizeTracker");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTSizeTracker$NoopNBTTracker",
					"net.minecraft.nbt.NBTSizeTracker$NoopNBTTracker");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagByte", "net.minecraft.nbt.NBTTagByte");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagCompound",
					"net.minecraft.nbt.NBTTagCompound");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagDouble",
					"net.minecraft.nbt.NBTTagDouble");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagEnd", "net.minecraft.nbt.NBTTagEnd");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagFloat", "net.minecraft.nbt.NBTTagFloat");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagInt", "net.minecraft.nbt.NBTTagInt");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagByteArray",
					"net.minecraft.nbt.NBTTagByteArray");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagIntArray",
					"net.minecraft.nbt.NBTTagIntArray");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagList", "net.minecraft.nbt.NBTTagList");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagLong", "net.minecraft.nbt.NBTTagLong");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagShort", "net.minecraft.nbt.NBTTagShort");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.MyNBTTagString",
					"net.minecraft.nbt.NBTTagString");
			typesToReplace.put("org.blockartistry.mod.Restructured.nbt.NBTFactory",
					"net.minecraft.nbt.NBTFactory");
		}

		// Obsfucation mapping - yay obsfucation!
		// RegionFileCache
		obsRemap.put("createOrLoadRegionFile", "func_76550_a");
		obsRemap.put("clearRegionFileReferences", "func_76551_a");
		obsRemap.put("getChunkInputStream", "func_76549_c");
		obsRemap.put("getChunkOutputStream", "func_76552_d");

		// RegionFile
		obsRemap.put("getChunkDataInputStream", "func_76704_a");
		obsRemap.put("getChunkDataOutputStream", "func_76710_b");

		// ThreadedFileIOBase
		obsRemap.put("queueIO", "func_75735_a");
		obsRemap.put("waitForFinish", "func_75734_a");
		obsRemap.put("threadedIOInstance", "field_75741_a");

		// AnvilChunkLoader
		obsRemap.put("chunkSaveLocation", "field_75825_d");
	}

	//private byte[] getClassBytes(final Class<?> clazz) {
	private byte[] getClassBytes(final String clazz) {

		try {
			String name = "org/blockartistry/mod/Restructured/" + clazz + ".class"; // clazz.getName().replace('.', '/') + ".class";
			InputStream stream = Transformer.class.getClassLoader().getResourceAsStream(name);
			final byte[] result = new byte[stream.available()];
			stream.read(result);
			return result;
		} catch (final Exception e) {
			System.out.println("Error getting class information");
		}

		return null;
	}

	private boolean verifyClassBytes(final byte[] bytes) {
		
		return true;
		/*
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		CheckClassAdapter.verify(new ClassReader(bytes), false, pw);
		final String result = sw.toString();
		if (result.length() > 0)
			System.out.print(result);
		return result.length() == 0;
		*/
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {

		final String replaceClass = targets.get(name);
		if (replaceClass != null) {
			System.out.println(String.format("Redefining '%s' with '%s'...", name, replaceClass));
			final ClassReader reader = new ClassReader(getClassBytes(replaceClass));
			final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
			final RenameMapper mapper = new RenameMapper(typesToReplace,
					TransformLoader.runtimeDeobEnabled ? obsRemap : null);
			final RemappingClassAdapter adapter = new RemappingClassAdapter(writer, mapper);
			reader.accept(adapter, ClassReader.EXPAND_FRAMES);
			final byte[] result = writer.toByteArray();
			if (verifyClassBytes(result)) {
				System.out.println(String.format("'%s' - SUCCESS!", name));
				return result;
			}
		}

		return basicClass;
	}
}
