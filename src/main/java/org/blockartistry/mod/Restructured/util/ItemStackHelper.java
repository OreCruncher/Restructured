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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public final class ItemStackHelper {
	
	static Random rand = new Random();

	public static ItemStack getItemStack(String name) {
		return getItemStack(name, 1);
	}

	public static ItemStack getItemStack(String name, int quantity) {

		ItemStack result = null;
		
		// Parse out the possible subtype from the end of the string
		String workingName = name;
		int subType = -1;

		if (StringUtils.countMatches(name, ":") == 2) {
			workingName = StringUtils.substringBeforeLast(name, ":");
			String num = StringUtils.substringAfterLast(name, ":");

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
		ArrayList<ItemStack> ores = OreDictionary.getOres(workingName);
		if (!ores.isEmpty()) {
			result = ores.get(0).copy();
			result.stackSize = quantity;
		} else {
			Item i = GameData.getItemRegistry().getObject(workingName);
			if (i != null) {
				result = new ItemStack(i, quantity);
			}
		}

		// If we did have a hit on a base item, set the subtype
		// as needed.
		if (result != null && subType != -1) {
			result.setItemDamage(subType);
		}

		// Log if we didn't find an item - it's possible that the recipe has a
		// type
		// or the mod has changed where the item no longer exists.
		// if (result == null)
		// ModLog.info("Unable to locate item: " + name);

		return result;
	}

	public static List<ItemStack> getItemStackRange(String name,
			int startSubtype, int endSubtype, int quantity) {

		return getItemStackRange(getItemStack(name).getItem(), startSubtype,
				endSubtype, quantity);
	}

	public static List<ItemStack> getItemStackRange(Item item, int start,
			int end, int quantity) {

		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (int i = start; i <= end; i++) {
			result.add(new ItemStack(item, quantity, i));
		}

		return result;
	}

	public static List<ItemStack> getItemStackRange(Block block, int start,
			int end, int quantity) {

		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (int i = start; i <= end; i++) {
			result.add(new ItemStack(block, quantity, i));
		}

		return result;
	}

	public static FluidStack getFluidStack(String name, int quantity) {
		return FluidRegistry.getFluidStack(name, quantity);
	}

	public static String resolveName(ItemStack stack) {
		String result = null;

		if (stack != null) {

			try {
				result = stack.getDisplayName();
			} catch (Exception e) {
				;
			}

			if (result == null) {
				try {
					result = stack.getUnlocalizedName();
				} catch (Exception e) {
					;
				}
			}
		}

		return result == null || result.isEmpty() ? "UNKNOWN" : result;
	}

	public void dumpSubItems(Logger log, String itemId) {
		ItemStack stack = getItemStack(itemId, 1);
		if (stack != null) {

			try {
				for (int i = 0; i < 1024; i++) {
					stack.setItemDamage(i);
					String name = resolveName(stack);
					if (name != null && !name.isEmpty()
							&& !name.contains("(Destroy)"))
						log.info(String.format("%s:%d = %s", itemId, i, name));
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				;
			}
		}
	}

	public void dumpItemStack(Logger log, String title, ItemStack... items) {

		log.info("");
		log.info(title);
		log.info(StringUtils.repeat('-', 32));

		if (items == null || items.length == 0) {
			log.info("No items in list");
			return;
		}

		for (ItemStack stack : items) {
			log.info(String.format("%s (%s)", resolveName(stack),
					stack.toString()));
		}
		log.info(StringUtils.repeat('-', 32));
		log.info(String.format("Total: %d item stacks", items.length));
	}

	public static void dumpFluidRegistry(Logger log) {

		log.info("Fluid Registry:");

		for (Entry<String, Fluid> e : FluidRegistry.getRegisteredFluids()
				.entrySet()) {
			log.info(String.format("%s: %s", e.getKey(), e.getValue().getName()));
		}
	}

	public static List<ItemStack> clone(ItemStack... stacks) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();
		for (int i = 0; i < stacks.length; i++)
			if (stacks[i] != null)
				result.add(stacks[i].copy());
		return result;
	}

	public static List<ItemStack> clone(List<ItemStack> stacks) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();
		for (ItemStack stack : stacks)
			if (stack != null)
				result.add(stack.copy());
		return result;
	}

	public static void spawnIntoWorld(World world, ItemStack stack, int x,
			int y, int z) {

		if (stack == null)
			return;

		float f = rand.nextFloat() * 0.8F + 0.1F;
		float f1 = rand.nextFloat() * 0.8F + 0.1F;
		float f2 = rand.nextFloat() * 0.8F + 0.1F;

		while (stack.stackSize > 0) {
			int j = rand.nextInt(21) + 10;

			if (j > stack.stackSize) {
				j = stack.stackSize;
			}

			stack.stackSize -= j;

			EntityItem item = new EntityItem(world, x + f, y + f1, z + f2,
					new ItemStack(stack.getItem(), j, stack.getItemDamage()));

			if (stack.hasTagCompound()) {
				item.getEntityItem().setTagCompound(
						(NBTTagCompound) stack.getTagCompound().copy());
			}

			world.spawnEntityInWorld(item);
		}
	}

	public static void setItemLore(ItemStack stack, String lore) {

		if (stack == null)
			return;

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();

		NBTTagList l = new NBTTagList();
		l.appendTag(new NBTTagString(lore));
		NBTTagCompound display = new NBTTagCompound();
		display.setTag("Lore", l);

		nbt.setTag("display", display);
		stack.setTagCompound(nbt);

	}

	public static ItemStack asGeneric(ItemStack stack) {
		return asGeneric(stack.getItem());
	}

	public static ItemStack asGeneric(Item item) {
		return new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
	}
}
