/*
 * This file is part of ThermalRecycling, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.Restructured.util;

import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.oredict.OreDictionary;

public final class ItemStackHelper {

	static Random rand = new Random();

	public static ItemStack getItemStack(final String name) {
		return getItemStack(name, 1);
	}

	public static ItemStack getItemStack(final String name, final int quantity) {

		ItemStack result = null;

		// Parse out the possible subtype from the end of the string
		String workingName = name;
		int subType = -1;

		if (StringUtils.countMatches(name, ":") == 2) {
			workingName = StringUtils.substringBeforeLast(name, ":");
			final String num = StringUtils.substringAfterLast(name, ":");

			if (num != null && !num.isEmpty()) {

				if ("*".compareTo(num) == 0)
					subType = OreDictionary.WILDCARD_VALUE;
				else {
					try {
						subType = Integer.parseInt(num);
					} catch (Exception e) {
						// It appears malformed - assume the incoming name
						// is
						// the real name and continue.
						;
					}
				}
			}
		}

		// Check the OreDictionary first for any alias matches. Otherwise
		// go to the game registry to find a match.
		final List<ItemStack> ores = OreDictionary.getOres(workingName);
		if (!ores.isEmpty()) {
			result = ores.get(0).copy();
			result.stackSize = quantity;
		} else {
			final Item i = GameData.getItemRegistry().getObject(new ResourceLocation(workingName));
			if (i != null) {
				result = new ItemStack(i, quantity);
			}
		}

		// If we did have a hit on a base item, set the subtype
		// as needed.
		if (result != null && subType != -1) {
			result.setItemDamage(subType);
		}

		return result;
	}
}
