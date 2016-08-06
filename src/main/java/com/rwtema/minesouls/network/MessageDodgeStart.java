package com.rwtema.minesouls.network;

import com.rwtema.minesouls.playerHandler.PlayerHandler;
import com.rwtema.minesouls.playerHandler.PlayerHandlerRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageDodgeStart extends NetworkHandler.Message {
	public MessageDodgeStart() {
		super();
	}

	@Override
	public NetworkHandler.Message runServer(MessageContext ctx) {

		EntityPlayerMP playerMP = ctx.getServerHandler().playerEntity;
		playerMP.getServerWorld().addScheduledTask(() -> {
			PlayerHandler handler = PlayerHandlerRegistry.INSTANCE.getPlayerHandler(playerMP);
			handler.startDodge();
		});
		return null;
	}

	@Override
	public void readData(PacketBuffer buffer) {

	}

	@Override
	protected void writeData(PacketBuffer buffer) {

	}
}
