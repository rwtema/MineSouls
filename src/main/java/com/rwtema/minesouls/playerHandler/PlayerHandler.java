package com.rwtema.minesouls.playerHandler;

import com.rwtema.minesouls.Helper;
import com.rwtema.minesouls.config.DifficultyConfig;
import com.rwtema.minesouls.network.MessagePlayerHandlerStats;
import com.rwtema.minesouls.network.NetworkHandler;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerHandler implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {

	public static final UUID dodgeID = UUID.fromString("c0dc01c8-8a4a-4ad4-af9f-2d83da0c8d6f");
	public final EntityPlayer player;
	public int enduranceCooldown = 0;
	public float poise = 0;
	public int poiseCooldown = 0;
	public float endurance = DifficultyConfig.MAX_ENDURANCE;
	public int staggeredTimer = 0;
	public int dodgeTimer = 0;
	public float dodgeF = 0;
	public float dodgeS = 0;
	public float dodgeY = 0;
	public float dodgeP = 0;
	boolean dirty = false;
	boolean wasPressingLeftClick;

	public PlayerHandler(EntityPlayer player) {
		super();
		this.player = player;
	}

	public void onJump() {
		endurance -= DifficultyConfig.ENDURANCE_COST_PER_JUMP;
		enduranceCooldown = (int) Math.max(enduranceCooldown, DifficultyConfig.ENDURANCE_COST_PER_JUMP / DifficultyConfig.ENDURANCE_COOLDOWN_ACTION_MODIFIER);
		dirty = true;
		checkEndurance();
	}

	public void attackPrevent(AttackEntityEvent event) {
		if (dodgeTimer > 0 || staggeredTimer > 0 || player.getCooledAttackStrength(1) != 1 || endurance < DifficultyConfig.ENDURANCE_COST_PER_ATTACK || enduranceCooldown > 0) {
			event.setCanceled(true);
		} else {
			endurance -= DifficultyConfig.ENDURANCE_COST_PER_ATTACK;
			enduranceCooldown = (int) Math.max(enduranceCooldown, DifficultyConfig.ENDURANCE_COST_PER_ATTACK / DifficultyConfig.ENDURANCE_COOLDOWN_ACTION_MODIFIER);
			dirty = true;
			checkEndurance();
		}
	}

	public void tick() {
		if (player.getHealth() <= 0) {
			poise = 0;
			endurance = 0;
			staggeredTimer = 0;
			poiseCooldown = 0;
			enduranceCooldown = 0;
			dodgeTimer = 0;
			return;
		}

		if (poiseCooldown > 0) {
			poiseCooldown--;
			if (poiseCooldown == 0) {
				poise = 0;
				dirty = true;
			}
		}

		if (staggeredTimer > 0) {
			player.resetCooldown();
			player.setSprinting(false);
			staggeredTimer--;
			if (staggeredTimer == 0) {
				dirty = true;
			}
		}

		if (dodgeTimer > 0) {
			dodgeTimer--;
		}

		if (endurance <= 0) {
			player.setSprinting(false);
		}

		if (enduranceCooldown > 0) {
			enduranceCooldown--;
			if (enduranceCooldown <= 0) {
				enduranceCooldown = 0;
				endurance = Math.max(endurance, DifficultyConfig.ENDURANCE_REGAIN_FROM_ZERO);
			}


		} else {
			if (player.isSprinting()) {
				if (endurance > 0) dirty = true;
				endurance -= DifficultyConfig.ENDURANCE_LOSS_SPRINTING;
			} else {
				if (endurance < DifficultyConfig.MAX_ENDURANCE) dirty = true;
				if (player.isActiveItemStackBlocking() || player.getFoodStats().getFoodLevel() < 6) {
					endurance += DifficultyConfig.ENDURANCE_GAIN_REDUCED;
				} else {
					endurance += DifficultyConfig.ENDURANCE_GAIN_REGULAR;
				}
			}
		}
		checkDodgeSpeed();
		checkEndurance();
	}

	private void checkEndurance() {
		if (endurance <= 0) {
			endurance = 0;
			if (enduranceCooldown == 0)
				enduranceCooldown = Math.max(enduranceCooldown, DifficultyConfig.EMPTY_ENDURANCE_COOLDOWN);
			player.setSprinting(false);
		} else if (endurance > DifficultyConfig.MAX_ENDURANCE) {
			endurance = DifficultyConfig.MAX_ENDURANCE;
		}

		if (dirty) {
			if (player instanceof EntityPlayerMP) {
				NetworkHandler.network.sendTo(new MessagePlayerHandlerStats(this), ((EntityPlayerMP) player));
			}
			dirty = false;
		}
	}

	public void attacked(LivingAttackEvent event) {
		if (dodgeTimer > 0 && !event.getSource().isUnblockable()) {
			event.setCanceled(true);
		}
	}

	public void hurt(LivingHurtEvent event) {
		DamageSource source = event.getSource();
		float amount = event.getAmount();

		if (amount > 32768) return; // EEP

		if (amount <= 0) return;


		if (endurance == 0) {
			amount *= DifficultyConfig.NO_ENDURANCE_DAMAGE_MODIFIER;
		}

		if (staggeredTimer > 0) {
			amount *= DifficultyConfig.STAGGER_DAMAGE_MODIFIER;
		}

		if (endurance > 0 && staggeredTimer == 0 && !source.isUnblockable() && player.isActiveItemStackBlocking()) {
			float block = endurance / DifficultyConfig.BLOCKING_ENDURANCE_MODIFIER;
			if (block > amount) {
				endurance -= amount * DifficultyConfig.BLOCKING_ENDURANCE_MODIFIER;
				amount = 0;
			} else {
				amount -= block;
				endurance = 0;
			}
		}

		if (staggeredTimer == 0 && amount > 0) {
			poiseCooldown = DifficultyConfig.POISE_COOLDOWN;
			float poiseToAdd = amount;

			poiseToAdd *= DifficultyConfig.POISE_LOSS_BASE_MODIFIER;

			if (player.isSneaking()) {
				poiseToAdd *= DifficultyConfig.POISE_PROTECTION_SNEAK_MODIFIER;
			}

			if (!source.isUnblockable()) {
				Entity entity = source.getEntity();
				if (source.getSourceOfDamage() != null) {
					if (entity != source.getSourceOfDamage()) {
						poiseToAdd *= DifficultyConfig.POISE_LOSS_PROJECTILE_MODIFIER;
					}
					entity = source.getSourceOfDamage();
				}

				if (entity instanceof EntityLivingBase) {
					ItemStack stack = ((EntityLivingBase) entity).getHeldItemMainhand();
					if (stack == null) {
						poiseToAdd *= DifficultyConfig.POISE_LOSS_BARE_HAND_MODIFIER;
					} else {
						if (Helper.isItemOfType(stack, Helper.ToolType.TOOL)) {
							poiseToAdd *= DifficultyConfig.POISE_LOSS_ANY_TOOL_MODIFIER;

							if (Helper.isSword(stack)) {
								poiseToAdd *= DifficultyConfig.POISE_LOSS_SWORD_MODIFIER;
							}
							if (Helper.isItemOfType(stack, Helper.ToolType.AXE)) {
								poiseToAdd *= DifficultyConfig.POISE_LOSS_AXE_MODIFIER;
							}
							if (Helper.isItemOfType(stack, Helper.ToolType.PICKAXE)) {
								poiseToAdd *= DifficultyConfig.POISE_LOSS_PICKAXE_MODIFIER;
							}
							if (Helper.isItemOfType(stack, Helper.ToolType.SHOVEL)) {
								poiseToAdd *= DifficultyConfig.POISE_LOSS_SHOVEL_MODIFIER;
							}
							if (Helper.isItemOfType(stack, Helper.ToolType.HEAVYTOOL)) {
								poiseToAdd *= DifficultyConfig.POISE_LOSS_LARGE_TOOL_MODIFIER;
							}
						}
					}
				}
			}

			poise += poiseToAdd;

			float poiseBreakPoint = getPoiseBreakPoint();
			if (poise > poiseBreakPoint) {
				staggeredTimer = Math.min(10 + (int) Math.ceil(4 * (poise - poiseBreakPoint)), DifficultyConfig.STAGGER_TIME);
				poise = poiseBreakPoint;
			}
		}

		if (staggeredTimer == 0 && amount > 0 && !source.isUnblockable()) {
			if (player.isHandActive()) {
				ItemStack stack = player.getActiveItemStack();
				if (stack != null && stack.getItem().getItemUseAction(stack) == EnumAction.EAT) {
					player.resetActiveHand();
				}
			}
		}

		dirty = true;
		checkEndurance();
		event.setAmount(amount);
	}

	public float getPoiseBreakPoint() {
		return (ForgeHooks.getTotalArmorValue(player)) * DifficultyConfig.POISE_ARMOR_MODIFIER;
	}

	@SideOnly(Side.CLIENT)
	public void clientTick() {
		Minecraft mc = Minecraft.getMinecraft();
		boolean pressingLeftClick = mc.gameSettings.keyBindAttack.isKeyDown();
		if (Helper.isSword(player.getHeldItemMainhand()) && player.getCooledAttackStrength(1) == 1 && pressingLeftClick && wasPressingLeftClick) {
			Entity entity = mc.pointedEntity;
			if (entity != null && mc.playerController != null) {
				mc.playerController.attackEntity(player, entity);
				player.swingArm(EnumHand.MAIN_HAND);
			}
		}
		wasPressingLeftClick = pressingLeftClick;

		checkDodgeSpeed();
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("enduranceCooldown", enduranceCooldown);
		nbt.setInteger("poiseCooldown", poiseCooldown);
		nbt.setFloat("endurance", endurance);
		nbt.setFloat("poise", poise);
		nbt.setInteger("staggeredTimer", staggeredTimer);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		enduranceCooldown = nbt.getInteger("enduranceCooldown");
		poiseCooldown = nbt.getInteger("poiseCooldown");
		staggeredTimer = nbt.getInteger("staggeredTimer");
		poise = nbt.getFloat("poise");
		endurance = nbt.getFloat("endurance");
		dirty = true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == PlayerHandlerRegistry.capability;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == PlayerHandlerRegistry.capability ? PlayerHandlerRegistry.capability.cast(this) : null;
	}

	public void startDodge() {
		dodgeTimer = DifficultyConfig.DODGE_TIME;
		endurance -= DifficultyConfig.ENDURANCE_COST_PER_DODGE;
		enduranceCooldown = (int) Math.max(enduranceCooldown, DifficultyConfig.ENDURANCE_COST_PER_DODGE / DifficultyConfig.ENDURANCE_COOLDOWN_ACTION_MODIFIER);
		dirty = true;
		checkDodgeSpeed();
		checkEndurance();
	}

	public void checkDodgeSpeed() {
		IAttributeInstance instance = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		AttributeModifier modifier = instance.getModifier(dodgeID);
		if (dodgeTimer <= 0) {
			if (modifier != null)
				instance.removeModifier(modifier);
		} else {
			if (modifier == null)
				instance.applyModifier(new AttributeModifier(dodgeID, "dodge", DifficultyConfig.DODGE_SPEED - 1, 2));
		}
	}

	@SideOnly(Side.CLIENT)
	public void renderTick() {
		if (dodgeTimer > 0) {
			player.rotationYaw = player.prevRotationYaw = dodgeY;
		}
	}


}
