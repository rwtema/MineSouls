package com.rwtema.minesouls.compat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.rwtema.minesouls.Helper;
import java.util.Set;
import net.minecraft.item.Item;
// import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.melee.TinkerMeleeWeapons;
import slimeknights.tconstruct.tools.ranged.TinkerRangedWeapons;
import slimeknights.tconstruct.tools.harvest.TinkerHarvestTools;

public class TCCompat {
	public static void init() {
		HashMultimap<Helper.ToolType, Item> tools = HashMultimap.create();
		tools.putAll(Helper.ToolType.SWORD, ImmutableList.of(
				TinkerMeleeWeapons.cleaver,
				TinkerMeleeWeapons.broadSword,
//				TinkerMeleeWeapons.cutlass,
//				TinkerMeleeWeapons.dagger,
				TinkerMeleeWeapons.longSword,
				TinkerMeleeWeapons.rapier));

		tools.putAll(Helper.ToolType.AXE, ImmutableList.of(
				TinkerMeleeWeapons.battleAxe,
				TinkerHarvestTools.lumberAxe,
				TinkerHarvestTools.hatchet
//				TinkerHarvestTools.scythe
		));

		tools.putAll(Helper.ToolType.PICKAXE, ImmutableList.of(
				TinkerHarvestTools.pickaxe,
				TinkerHarvestTools.hammer
		));

		tools.putAll(Helper.ToolType.SHOVEL, ImmutableList.of(
				TinkerHarvestTools.shovel,
				TinkerHarvestTools.mattock));

		tools.putAll(Helper.ToolType.HEAVYTOOL, ImmutableList.of(
				TinkerHarvestTools.hammer,
				TinkerHarvestTools.excavator,
				TinkerHarvestTools.lumberAxe,
				TinkerMeleeWeapons.cleaver
//				TinkerMeleeWeapons.battleAxe,
//				TinkerMeleeWeapons.scythe
		));

		tools.putAll(Helper.ToolType.HEAVYTOOL, ImmutableList.of(
				TinkerHarvestTools.pickaxe,
				TinkerHarvestTools.shovel,
				TinkerHarvestTools.hatchet,
				TinkerHarvestTools.mattock,
				TinkerMeleeWeapons.broadSword,
				TinkerMeleeWeapons.longSword,
				TinkerMeleeWeapons.rapier,
//				TinkerMeleeWeapons.cutlass,
//				TinkerMeleeWeapons.dagger,
				TinkerMeleeWeapons.fryPan,
				TinkerMeleeWeapons.battleSign,

				TinkerHarvestTools.hammer,
				TinkerHarvestTools.excavator,
				TinkerHarvestTools.lumberAxe,
				TinkerMeleeWeapons.cleaver,
//				TinkerMeleeWeapons.battleAxe,
//				TinkerHarvestTools.scythe,

				TinkerRangedWeapons.shuriken
		));

		for (Helper.ToolType type : Helper.ToolType.values()) {
			Set<Item> set = tools.get(type);
			Helper.registerPredicate(type, (item, stack) -> set.contains(item));
		}
	}
}
