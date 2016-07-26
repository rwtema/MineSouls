package com.rwtema.minesouls;

import com.google.common.collect.HashMultimap;
import java.util.Collection;
import java.util.function.BiPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

public class Helper {
	static HashMultimap<ToolType, BiPredicate<Item, ItemStack>> predicates = HashMultimap.create();

	static {
		predicates.put(ToolType.TOOL, (item, itemStack) -> item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemFishingRod);
		predicates.put(ToolType.SWORD, (item, itemStack) -> item instanceof ItemSword);
		predicates.put(ToolType.AXE, (item, itemStack) -> item instanceof ItemAxe);
		predicates.put(ToolType.PICKAXE, (item, itemStack) -> item instanceof ItemPickaxe);
		predicates.put(ToolType.SHOVEL, (item, itemStack) -> item instanceof ItemSpade);
	}

	public static boolean isSword(ItemStack stack) {
		return isItemOfType(stack, ToolType.SWORD);
	}

	public static boolean isItemOfType(ItemStack stack, ToolType type) {
		return isItemOfType(stack, predicates.get(type));
	}

	private static boolean isItemOfType(ItemStack stack, Collection<BiPredicate<Item, ItemStack>> predicates) {
		if (stack == null) return false;
		Item item = stack.getItem();
		//noinspection ConstantConditions
		if (item == null) return false;

		for (BiPredicate<Item, ItemStack> predicate : predicates) {
			if (predicate.test(item, stack)) {
				return true;
			}
		}

		return false;
	}

	public static void registerPredicate(ToolType type, BiPredicate<Item, ItemStack> biPredicate) {
		predicates.put(type, biPredicate);
	}

	public enum ToolType {
		TOOL,
		SWORD,
		PICKAXE,
		AXE,
		SHOVEL,
		HEAVYTOOL
	}
}
