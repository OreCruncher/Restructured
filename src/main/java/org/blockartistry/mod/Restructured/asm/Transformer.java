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

import org.blockartistry.mod.Restructured.chunk.MyRegionFile;
import org.blockartistry.mod.Restructured.chunk.MyThreadedFileIOBase;
import org.blockartistry.mod.Restructured.chunk.RegionFileLRU;
import org.blockartistry.mod.Restructured.chunk.ChunkInputStream;
import org.blockartistry.mod.Restructured.chunk.ChunkOutputStream;
import org.blockartistry.mod.Restructured.chunk.MyAnvilChunkLoader;
import org.blockartistry.mod.Restructured.chunk.MyChunkBuffer;
import org.blockartistry.mod.Restructured.chunk.MyRegionCache;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer {

	private static Map<String, Class<?>> targets = new HashMap<String, Class<?>>();
	private static Map<String, String> typesToReplace = new HashMap<String, String>();
	private static Map<String, String> obsRemap = new HashMap<String, String>();

	static {

		targets.put("net.minecraft.world.chunk.storage.RegionFileCache", MyRegionCache.class);
		targets.put("net.minecraft.world.chunk.storage.RegionFile", MyRegionFile.class);
		targets.put("net.minecraft.world.chunk.storage.ChunkBuffer", MyChunkBuffer.class);
		targets.put("net.minecraft.world.chunk.storage.RegionFileLRU", RegionFileLRU.class);
		targets.put("net.minecraft.world.storage.ThreadedFileIOBase", MyThreadedFileIOBase.class);
		targets.put("net.minecraft.world.storage.ChunkOutputStream", ChunkOutputStream.class);
		targets.put("net.minecraft.world.storage.ChunkInputStream", ChunkInputStream.class);
		targets.put("net.minecraft.world.chunk.storage.AnvilChunkLoader", MyAnvilChunkLoader.class);

		targets.put("aqj", MyRegionCache.class);
		targets.put("aqh", MyRegionFile.class);
		targets.put("azr", MyThreadedFileIOBase.class);
		targets.put("aqk", MyAnvilChunkLoader.class);

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
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.MyThreadedFileIOBase",
				"net.minecraft.world.storage.ThreadedFileIOBase");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.ChunkOutputStream",
				"net.minecraft.world.storage.ChunkOutputStream");
		typesToReplace.put("org.blockartistry.mod.Restructured.chunk.ChunkInputStream",
				"net.minecraft.world.storage.ChunkInputStream");

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

	private byte[] getClassBytes(final Class<?> clazz) {

		try {
			String name = clazz.getName().replace('.', '/') + ".class";
			InputStream stream = clazz.getClassLoader().getResourceAsStream(name);
			final byte[] result = new byte[stream.available()];
			stream.read(result);
			return result;
		} catch (final Exception e) {
			System.out.println("Error getting class information");
		}

		return null;
	}

	private boolean verifyClassBytes(final byte[] bytes) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		CheckClassAdapter.verify(new ClassReader(bytes), false, pw);
		final String result = sw.toString();
		if (result.length() > 0)
			System.out.print(result);
		return result.length() == 0;
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {

		final Class<?> replaceClass = targets.get(name);
		if (replaceClass != null) {
			System.out.println(String.format("Redefining '%s'...", name));
			final ClassReader reader = new ClassReader(getClassBytes(replaceClass));
			final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
			final RenameMapper mapper = new RenameMapper(typesToReplace,
					TransformLoader.runtimeDeobEnabled ? obsRemap : null);
			final MyRemappingClassAdapter adapter = new MyRemappingClassAdapter(writer, mapper);
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
