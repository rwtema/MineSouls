package com.rwtema.minesouls;

import com.rwtema.minesouls.config.DifficultyConfig;
import com.rwtema.minesouls.playerHandler.PlayerHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
public class MineSoulsControllerHooks {
	public static boolean shouldSlowPlayer(EntityPlayer player) {
		if (!MineSouls.isMineSoulsOnServer()) return player.isHandActive();
		return isPlayerUsingButNotBlocking(player) || PlayerHandlerRegistry.INSTANCE.getPlayerHandler(player).staggeredTimer > DifficultyConfig.STAGGER_SLOW_TIME;
	}

	public static boolean isPlayerUsingOrHoldingSword(EntityPlayer player) {
		if (player.isHandActive())
			return true;

		ItemStack heldItemMainhand = player.getHeldItemMainhand();
		if (Helper.isSword(heldItemMainhand)) {
			Minecraft minecraft = Minecraft.getMinecraft();
			RayTraceResult trace = minecraft.objectMouseOver;
			return trace == null || trace.typeOfHit != RayTraceResult.Type.BLOCK || minecraft.theWorld.getBlockState(trace.getBlockPos()).getBlock() != Blocks.WEB;
		}

		return false;
	}

	public static boolean isPlayerUsingButNotBlocking(EntityPlayer player) {
		if (player.isHandActive()) {
			ItemStack stack = player.getActiveItemStack();
			if (stack == null || stack.getItem().getItemUseAction(stack) != EnumAction.BLOCK) {
				return true;
			}
		}
		return false;
	}

	public static float getAttackStrengthModifier(EntityPlayer player, float adjustTicks) {
		float v = player.getCooledAttackStrength(adjustTicks);
		if (!MineSouls.isMineSoulsOnServer()) return v;
		return v < 1 ? 0 : 1;
	}

	public static void dontResetCooldown(EntityPlayer player) {
		if (!MineSouls.isMineSoulsOnServer()) player.resetCooldown();
	}

	public static void resetCooldownIfFull(EntityPlayer player) {
		if (!MineSouls.isMineSoulsOnServer() || player.getCooledAttackStrength(1) == 1)
			player.resetCooldown();
	}

	@SideOnly(Side.CLIENT)
	public static void swingArmIfFull(EntityPlayerSP player, EnumHand hand) {
		if (!MineSouls.isMineSoulsOnServer() || player.getCooledAttackStrength(1) == 1)
			player.swingArm(EnumHand.MAIN_HAND);
	}

	@SideOnly(Side.CLIENT)
	public static void attackEntity(PlayerControllerMP controllerMP, EntityPlayer playerIn, Entity targetEntity) {
		if (!MineSouls.isMineSoulsOnServer() || playerIn.getCooledAttackStrength(1) == 1)
			controllerMP.attackEntity(playerIn, targetEntity);
	}

	public static void addFoodStats(FoodStats foodStats, int foodLevelIn, float foodSaturationModifier) {
		foodStats.addStats(foodLevelIn, foodSaturationModifier);
		if (MineSouls.isMineSoulsOnServer()) {
			EntityPlayer player = PlayerHandlerRegistry.findFoodStatOwner(foodStats);
			if (player != null) {
				player.heal(foodLevelIn * DifficultyConfig.FOOD_HEAL_MODIFIER);
			}
		}
	}

	public static boolean canPlayerEat(EntityPlayer player, boolean ignoreHunger) {
		return player.canEat(ignoreHunger) || (MineSouls.isMineSoulsOnServer() && player.getHealth() < player.getMaxHealth() && DifficultyConfig.FOOD_HEAL_MODIFIER > 0);
	}

	public static void onFoodEaten(FoodStats foodStats, ItemFood foodItem, ItemStack stack) {
		foodStats.addStats(foodItem, stack);
		if (MineSouls.isMineSoulsOnServer()) {
			EntityPlayer player = PlayerHandlerRegistry.findFoodStatOwner(foodStats);
			if (player != null) {
				player.heal(foodItem.getHealAmount(stack));
			}
		}
	}


}
