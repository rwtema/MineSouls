package com.rwtema.minesouls.playerHandler;

import com.google.common.collect.MapMaker;
import com.rwtema.minesouls.MineSouls;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerHandlerRegistry {
	public static final PlayerHandlerRegistry INSTANCE = new PlayerHandlerRegistry();
	public static Map<FoodStats, EntityPlayer> foodStatOwners = new MapMaker().weakKeys().weakValues().makeMap();
	Map<EntityPlayer, PlayerHandler> handlers = new MapMaker().weakKeys().makeMap();

	public static void init() {
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	@Nullable
	public static EntityPlayer findFoodStatOwner(FoodStats foodStats) {
		return foodStatOwners.computeIfAbsent(foodStats, foodStats1 -> {
			for (EntityPlayer entityPlayer : INSTANCE.handlers.keySet()) {
				if (entityPlayer.getFoodStats() == foodStats)
					return entityPlayer;
			}

			return null;
		});
	}

	public Optional<PlayerHandler> getPlayerHandler(Entity entity) {
		if (entity instanceof EntityPlayer)
			return Optional.of(getPlayerHandler((EntityPlayer) entity));
		return Optional.empty();
	}

	public PlayerHandler getPlayerHandler(EntityPlayer player) {
		return handlers.computeIfAbsent(player, PlayerHandler::new);
	}

	@SubscribeEvent
	public void onJump(LivingEvent.LivingJumpEvent event) {
		if (MineSouls.isMineSoulsOnServer())
			getPlayerHandler(event.getEntity()).ifPresent(PlayerHandler::onJump);
	}

	@SubscribeEvent
	public void attacked(LivingHurtEvent event) {
		if (MineSouls.isMineSoulsOnServer())
			getPlayerHandler(event.getEntity()).ifPresent(playerHandler -> playerHandler.attacked(event));
	}

	@SubscribeEvent
	public void attackPrevent(AttackEntityEvent event) {
		if (MineSouls.isMineSoulsOnServer())
			getPlayerHandler(event.getEntity()).ifPresent(playerHandler -> playerHandler.attackPrevent(event));
	}

	@SubscribeEvent
	public void tick(TickEvent.PlayerTickEvent event) {
		if (MineSouls.isMineSoulsOnServer() && event.phase == TickEvent.Phase.START)
			getPlayerHandler(event.player).tick();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (MineSouls.isMineSoulsOnServer() && event.phase == TickEvent.Phase.START) {
			EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
			if (thePlayer != null)
				getPlayerHandler(thePlayer).clientTick();
		}
	}
}
