/*
 * This file is part of Restructured, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 * Copyright (c) contributors
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

package org.blockartistry.mod.Restructured.assets;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;

import net.minecraftforge.common.config.Configuration;

public final class ZipProcessor {

	private ZipProcessor() {
	}

	private static File[] getZipFiles(final File path) {
		return path.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return pathname.isFile() && pathname.getName().endsWith(".zip");
			}
		});		
	}
	
	private static void traverseZips(final File path, final Predicate<ZipEntry> test,
			final Predicate<Object[]> process) {

		for (final File file : getZipFiles(path)) {
			try {
				final ZipFile zip = new ZipFile(file);
				final String prefix = StringUtils.removeEnd(file.getName(), ".zip").toLowerCase().replaceAll("[-.]", "_");
				Enumeration<? extends ZipEntry> entries = zip.entries();
				while (entries.hasMoreElements()) {
					final ZipEntry entry = entries.nextElement();
					if (test.apply(entry)) {
						final InputStream stream = zip.getInputStream(entry);
						process.apply(new Object[] { prefix, entry, stream });
						stream.close();
					}
				}
				zip.close();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Processes the zip files in the specified directory and integrates the
	 * content into the Restructured system.
	 */
	public static void initialize(final File path, final Configuration schematics, final Configuration chests,
			final List<SchematicProperties> props) {
		traverseZips(path, new ConfigProcessor.ChestsConfigFilter(), new ConfigProcessor.ChestsConfigProcess(chests));
		traverseZips(path, new ConfigProcessor.SchematicsConfigFilter(),
				new ConfigProcessor.SchematicsConfigProcess(schematics));
		traverseZips(path, new ConfigProcessor.SchematicFilter(),
				new ConfigProcessor.SchematicsProcess(schematics, props));
	}
	
	public static boolean areZipsPresent(final File path) {
		return getZipFiles(path).length > 0;
	}
}
