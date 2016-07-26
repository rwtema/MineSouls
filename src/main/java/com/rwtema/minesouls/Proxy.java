package com.rwtema.minesouls;

import com.rwtema.minesouls.network.NetworkHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class Proxy {
	public void onLoad() {

	}

	public NetworkHandler.Message runMessage(NetworkHandler.Message message, MessageContext ctx) {
		if (ctx.side == Side.CLIENT)
			return null;
		else {
			return message.runServer(ctx);
		}
	}

	public boolean isServer() {
		return true;
	}
}
