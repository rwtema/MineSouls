package com.rwtema.minesouls.network;


import com.rwtema.minesouls.MineSouls;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetworkHandler {
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(MineSouls.MODID);
	public static final IMessageHandler<Message, Message> handler = new Handler();


	public static void init() {
		network.registerMessage(handler, MessagePlayerHandlerStats.class, 0, Side.CLIENT);
	}

	public static class Handler implements IMessageHandler<Message, Message> {

		@Override
		public Message onMessage(Message message, MessageContext ctx) {
			return MineSouls.proxy.runMessage(message, ctx);
		}
	}

	public static abstract class Message implements IMessage {

		PacketBuffer buffer;

		public Message() {

		}

		@Override
		public void fromBytes(ByteBuf buf) {
			buffer = new PacketBuffer(buf);
			readData(buffer);

		}

		public abstract void readData(PacketBuffer buffer);

		@Override
		public void toBytes(ByteBuf buf) {
			writeData(new PacketBuffer(buf));
		}

		protected abstract void writeData(PacketBuffer buffer);

		public Message runServer(MessageContext ctx) {
			throw new IllegalStateException("Unexpected Side");
		}

		@SideOnly(Side.CLIENT)
		public Message runClient(MessageContext ctx) {
			throw new IllegalStateException("Unexpected Side");
		}
	}
}
