package com.rwtema.minesouls.key;

import com.rwtema.minesouls.MineSouls;
import com.rwtema.minesouls.config.DifficultyConfig;
import com.rwtema.minesouls.network.MessageDodgeStart;
import com.rwtema.minesouls.network.NetworkHandler;
import com.rwtema.minesouls.playerHandler.PlayerHandler;
import com.rwtema.minesouls.playerHandler.PlayerHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyHandler {

	public static KeyBinding dodge;

	public static void init() {
		dodge = new KeyBinding("key.movement", Keyboard.KEY_R, "key.categories.gameplay");
		dodge.setKeyConflictContext(new IKeyConflictContext() {
			@Override
			public boolean isActive() {
				return KeyConflictContext.IN_GAME.isActive();
			}

			@Override
			public boolean conflicts(IKeyConflictContext other) {
				return other == this || other == KeyConflictContext.IN_GAME;
			}
		});
		ClientRegistry.registerKeyBinding(dodge);
		MinecraftForge.EVENT_BUS.register(KeyHandler.class);
	}

	@SubscribeEvent
	static void tick(InputEvent event) {
		if (Keyboard.getEventKeyState() && MineSouls.isMineSoulsOnServer()) {
			int eventKey = Keyboard.getEventKey();
			if (dodge.isActiveAndMatches(eventKey)) {
				EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
				if (player == null) return;

				PlayerHandler handler = PlayerHandlerRegistry.INSTANCE.getPlayerHandler(player);
				if (handler.dodgeTimer > 0 || handler.endurance == 0) return;

				float s = player.movementInput.moveStrafe;
				float f = player.movementInput.moveForward;
				float d = s * s + f * f;
				if (d > 0.01) {
					d = MathHelper.sqrt_float(d);
					s /= d;
					f /= d;
					handler.dodgeS = s;
					handler.dodgeF = f;
					handler.dodgeP = player.rotationPitch;
					handler.dodgeY = player.rotationYaw;
					handler.dodgeTimer = DifficultyConfig.DODGE_TIME;
					NetworkHandler.network.sendToServer(new MessageDodgeStart());
				}
			}
		}

	}


}
