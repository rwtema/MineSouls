package com.rwtema.minesouls.compat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.rwtema.minesouls.Helper;
import java.util.Set;
import net.minecraft.item.Item;
import slimeknights.tconstruct.tools.TinkerTools;

public class TCCompat {
	public static void init() {
		HashMultimap<Helper.ToolType, Item> tools = HashMultimap.create();
		tools.putAll(Helper.ToolType.SWORD, ImmutableList.of(
				TinkerTools.cleaver,
				TinkerTools.broadSword,
//				TinkerTools.cutlass,
//				TinkerTools.dagger,
				TinkerTools.longSword,
				TinkerTools.rapier));

		tools.putAll(Helper.ToolType.AXE, ImmutableList.of(
//				TinkerTools.battleAxe,
				TinkerTools.lumberAxe,
				TinkerTools.hatchet
//				TinkerTools.scythe
		));

		tools.putAll(Helper.ToolType.PICKAXE, ImmutableList.of(
				TinkerTools.pickaxe,
				TinkerTools.hammer
		));

		tools.putAll(Helper.ToolType.SHOVEL, ImmutableList.of(
				TinkerTools.shovel,
				TinkerTools.mattock));

		tools.putAll(Helper.ToolType.HEAVYTOOL, ImmutableList.of(
				TinkerTools.hammer,
				TinkerTools.excavator,
				TinkerTools.lumberAxe,
				TinkerTools.cleaver
//				TinkerTools.battleAxe,
//				TinkerTools.scythe
		));

		tools.putAll(Helper.ToolType.HEAVYTOOL, ImmutableList.of(
				TinkerTools.pickaxe,
				TinkerTools.shovel,
				TinkerTools.hatchet,
				TinkerTools.mattock,
				TinkerTools.broadSword,
				TinkerTools.longSword,
				TinkerTools.rapier,
//				TinkerTools.cutlass,
//				TinkerTools.dagger,
				TinkerTools.fryPan,
				TinkerTools.battleSign,

				TinkerTools.hammer,
				TinkerTools.excavator,
				TinkerTools.lumberAxe,
				TinkerTools.cleaver,
//				TinkerTools.battleAxe,
//				TinkerTools.scythe,

				TinkerTools.shuriken
		));

		for (Helper.ToolType type : Helper.ToolType.values()) {
			Set<Item> set = tools.get(type);
			Helper.registerPredicate(type, (item, stack) -> set.contains(item));
		}
	}
}
