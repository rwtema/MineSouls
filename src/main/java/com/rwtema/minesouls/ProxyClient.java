package com.rwtema.minesouls;

import com.rwtema.minesouls.hud.HUDHandler;
import com.rwtema.minesouls.key.KeyHandler;
import com.rwtema.minesouls.network.NetworkHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ProxyClient extends Proxy {
	@Override
	public void onLoad() {
		HUDHandler.init();
		KeyHandler.init();
	}

	@Override
	public NetworkHandler.Message runMessage(NetworkHandler.Message message, MessageContext ctx) {
		if (ctx.side == Side.CLIENT)
			return message.runClient(ctx);
		else
			return message.runServer(ctx);
	}

	@Override
	public boolean isServer() {
		return false;
	}

}
