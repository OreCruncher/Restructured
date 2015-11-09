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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.commons.Remapper;

public class RenameMapper extends Remapper {

	private final Map<String, String> mapping;
	private final Map<String, String> methodNameMap;

	private static String massage(final String s) {
		return s.replace('.', '/');
	}

	public RenameMapper(Map<String, String> mapping) {
		this(mapping, null);
	}

	public RenameMapper(Map<String, String> mapping, Map<String, String> methodMap) {
		this.mapping = new HashMap<String, String>();
		this.methodNameMap = methodMap;

		for (final Entry<String, String> e : mapping.entrySet())
			this.mapping.put(massage(e.getKey()), massage(e.getValue()));
	}

	public RenameMapper(String oldName, String newName) {
		this.mapping = Collections.singletonMap(massage(oldName), massage(newName));
		this.methodNameMap = null;
	}

	@Override
	public String mapMethodName(String owner, String name, String desc) {
		if (methodNameMap == null)
			return name;

		final String newName = methodNameMap.get(name);
		return newName == null ? name : newName;
	}

	@Override
	public String mapFieldName(String owner, String name, String desc) {
		if (methodNameMap == null)
			return name;

		final String newName = methodNameMap.get(name);
		return newName == null ? name : newName;
	}

	@Override
	public String mapType(String type) {
		return fix(type);
	}

	@Override
	public String map(String key) {
		return fix(key);
	}

	private String fix(String s) {
		if (s != null) {
			for (final Entry<String, String> entry : mapping.entrySet()) {
				if (s.indexOf(entry.getKey()) != -1) {
					s = s.replaceAll(entry.getKey(), entry.getValue());
				}
			}
		}
		return s;
	}
}