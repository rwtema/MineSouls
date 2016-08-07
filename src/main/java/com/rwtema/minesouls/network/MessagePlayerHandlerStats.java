package com.rwtema.minesouls.network;

import com.rwtema.minesouls.RunnableClient;
import com.rwtema.minesouls.playerHandler.PlayerHandler;
import com.rwtema.minesouls.playerHandler.PlayerHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessagePlayerHandlerStats extends NetworkHandler.Message {
	public float endurance;
	public int enduranceCooldown;
	public float poise;
	public int poiseCooldown;
	public int staggerTimer;

	PlayerHandler handler;

	public MessagePlayerHandlerStats() {

	}

	public MessagePlayerHandlerStats(PlayerHandler handler) {
		this.handler = handler;
	}

	@Override
	public void readData(PacketBuffer buffer) {
		endurance = buffer.readFloat();
		enduranceCooldown = buffer.readInt();
		poise = buffer.readFloat();
		poiseCooldown = buffer.readInt();
		staggerTimer = buffer.readInt();
	}

	@Override
	protected void writeData(PacketBuffer buffer) {
		buffer.writeFloat(handler.endurance);
		buffer.writeInt(handler.enduranceCooldown);
		buffer.writeFloat(handler.poise);
		buffer.writeInt(handler.poiseCooldown);
		buffer.writeInt(handler.staggeredTimer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public NetworkHandler.Message runClient(MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(new RunnableClient() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
				PlayerHandler handler = PlayerHandlerRegistry.INSTANCE.getPlayerHandler(thePlayer);
				handler.endurance = endurance;
				handler.enduranceCooldown = enduranceCooldown;
				handler.poise = poise;
				handler.poiseCooldown = poiseCooldown;
				handler.staggeredTimer = staggerTimer;
			}
		});
		return null;
	}
}
