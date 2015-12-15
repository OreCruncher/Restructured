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

import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.blockartistry.mod.Restructured.ModLog;

public abstract class SchematicFormat {

	public static final Map<String, SchematicFormat> FORMATS = new HashMap<String, SchematicFormat>();
	public static String FORMAT_DEFAULT;

	public abstract Schematic readFromNBT(NBTTagCompound tagCompound);

	public static Schematic readFromStream(InputStream stream) {
		try {
			final NBTTagCompound tagCompound = SchematicUtil
					.readTagCompoundFromStream(stream);
			final String format = tagCompound.getString(Names.NBT.MATERIALS);
			final SchematicFormat schematicFormat = FORMATS.get(format);

			if (schematicFormat == null) {
				throw new UnsupportedFormatException(format);
			}

			return schematicFormat.readFromNBT(tagCompound);
		} catch (final Exception ex) {
			ModLog.error("Failed to read schematic!", ex);
		}

		return null;
	}

	public static Schematic readFromFile(File file) {
		try {
			final NBTTagCompound tagCompound = SchematicUtil
					.readTagCompoundFromFile(file);
			final String format = tagCompound.getString(Names.NBT.MATERIALS);
			final SchematicFormat schematicFormat = FORMATS.get(format);

			if (schematicFormat == null) {
				throw new UnsupportedFormatException(format);
			}

			return schematicFormat.readFromNBT(tagCompound);
		} catch (Exception ex) {
			ModLog.error("Failed to read schematic!", ex);
		}

		return null;
	}

	public static Schematic readFromFile(File directory, String filename) {
		return readFromFile(new File(directory, filename));
	}

	static {
		FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());

		FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
	}
}
