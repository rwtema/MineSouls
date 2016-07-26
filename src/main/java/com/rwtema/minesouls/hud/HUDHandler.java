package com.rwtema.minesouls.hud;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rwtema.minesouls.MineSouls;
import com.rwtema.minesouls.config.DifficultyConfig;
import com.rwtema.minesouls.config.PersonalConfig;
import com.rwtema.minesouls.gui.GuiYouDied;
import com.rwtema.minesouls.playerHandler.PlayerHandler;
import com.rwtema.minesouls.playerHandler.PlayerHandlerRegistry;
import java.util.List;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HUDHandler {
	private final static ResourceLocation GUI_TEXTURE = new ResourceLocation(MineSouls.MODID, "textures/gui.png");
	public final int BORDER = 12;
	float partialTime = 0;
	float intensity = 0;
	List<ScrollBar> scrollBars;
	Set<RenderGameOverlayEvent.ElementType> preventDrawing;
	int time = 0;

	{
		scrollBars = ImmutableList.<ScrollBar>builder()
				.add(new ScrollBar(20, RenderGameOverlayEvent.ElementType.HEALTH, 0, 0, 0.9F) {
					@Override
					public float getCurrentValue(EntityPlayer player, PlayerHandler handler) {
						return player.getHealth() + player.getAbsorptionAmount();
					}

					@Override
					public float getMaxValue(EntityPlayer player, PlayerHandler handler) {
						return player.getMaxHealth() + player.getAbsorptionAmount();
					}

					@Override
					protected boolean isEmergency(EntityPlayerSP thePlayer, PlayerHandler handler) {
						return thePlayer.getHealth() < (thePlayer.getMaxHealth() / 5);
					}
				})
				.add(new ScrollBar(DifficultyConfig.MAX_ENDURANCE, null, 0.9F, 0, 0) {
					@Override
					public float getCurrentValue(EntityPlayer player, PlayerHandler handler) {
						return handler.endurance;
					}

					@Override
					protected boolean isEmergency(EntityPlayerSP thePlayer, PlayerHandler handler) {
						return handler.endurance == 0 || handler.staggeredTimer > 0;
					}

					@Override
					public boolean shouldRender(EntityPlayer player, PlayerHandler handler) {
						return MineSouls.isMineSoulsOnServer();
					}
				})
				.add(new ScrollBar(20, RenderGameOverlayEvent.ElementType.FOOD, 0.4117647F, 0, 0.9F) {
					@Override
					public float getCurrentValue(EntityPlayer player, PlayerHandler handler) {
						if (player.getHealth() <= 0) return 0;
						FoodStats stats = player.getFoodStats();
						return stats.getFoodLevel();
					}

					@Override
					protected boolean isEmergency(EntityPlayerSP thePlayer, PlayerHandler handler) {
						return getCurrentValue(thePlayer, handler) <= 6;
					}
				})
				.add(new ScrollBar(25, RenderGameOverlayEvent.ElementType.ARMOR, 231 / 255F, 240 / 255F, 230 / 255F) {
					@Override
					public float getCurrentValue(EntityPlayer player, PlayerHandler handler) {
						if (player.getHealth() <= 0) return 0;
						if(!MineSouls.isMineSoulsOnServer())
							return getMaxValue(player, handler);

						if (handler.staggeredTimer > 0) return 0;
						float poiseLevel = handler.poise / handler.getPoiseBreakPoint();
						return (1 - poiseLevel) * getMaxValue(player, handler);
					}

					@Override
					public float getMaxValue(EntityPlayer player, PlayerHandler handler) {
						return ForgeHooks.getTotalArmorValue(player);
					}

					@Override
					public boolean shouldRender(EntityPlayer player, PlayerHandler handler) {
						return ForgeHooks.getTotalArmorValue(player) > 0;
					}

					@Override
					protected boolean isEmergency(EntityPlayerSP thePlayer, PlayerHandler handler) {
						return thePlayer.getHealth() <= 0 || (MineSouls.isMineSoulsOnServer() && (handler.staggeredTimer > 0 || handler.poise >= handler.getPoiseBreakPoint()));
					}
				})
				.add(new ScrollBar(20, RenderGameOverlayEvent.ElementType.HEALTHMOUNT, 0, 0, 1) {
					@Override
					public float getCurrentValue(EntityPlayer player, PlayerHandler handler) {
						Entity tmp = player.getRidingEntity();
						if (!(tmp instanceof EntityLivingBase)) return 20;
						return ((EntityLivingBase) tmp).getHealth();
					}

					@Override
					public float getMaxValue(EntityPlayer player, PlayerHandler handler) {
						Entity tmp = player.getRidingEntity();
						if (!(tmp instanceof EntityLivingBase)) return 20;
						return ((EntityLivingBase) tmp).getMaxHealth();
					}

					@Override
					public boolean shouldRender(EntityPlayer player, PlayerHandler handler) {
						return player.getRidingEntity() instanceof EntityLivingBase;
					}

					@Override
					protected boolean isEmergency(EntityPlayerSP thePlayer, PlayerHandler handler) {
						Entity tmp = thePlayer.getRidingEntity();
						return tmp instanceof EntityLivingBase && ((EntityLivingBase) tmp).hurtResistantTime > 0;
					}
				})
				.add(new ScrollBar(300, RenderGameOverlayEvent.ElementType.AIR, 0, 1, 0) {

					@Override
					public float getCurrentValue(EntityPlayer player, PlayerHandler handler) {
						return Math.max(0, player.getAir());
					}

					@Override
					public boolean shouldRender(EntityPlayer player, PlayerHandler handler) {
						return player.isInsideOfMaterial(Material.WATER);
					}

					@Override
					protected boolean isEmergency(EntityPlayerSP thePlayer, PlayerHandler handler) {
						return thePlayer.getAir() <= 0;
					}
				})
				.build();

		ImmutableSet.Builder<RenderGameOverlayEvent.ElementType> builder = ImmutableSet.builder();
		builder.add(RenderGameOverlayEvent.ElementType.EXPERIENCE);
		for (ScrollBar scrollBar : scrollBars) {
			if (scrollBar.type != null) {
				builder.add(scrollBar.type);
			}
		}
		preventDrawing = builder.build();
	}

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new HUDHandler());
	}

	@SubscribeEvent
	public void guiOverride(GuiOpenEvent event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiGameOver) {
			event.setGui(new GuiYouDied(((GuiGameOver) gui).causeOfDeath));
		}
	}

	@SubscribeEvent
	public void renderPartial(TickEvent.RenderTickEvent event) {
		partialTime = event.renderTickTime;
		intensity = (float) (2 + Math.cos((time + partialTime) / 19 * Math.PI)) / 3;
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		time++;
		for (ScrollBar scrollBar : scrollBars) {
			scrollBar.clientTick();
		}
	}

	@SubscribeEvent
	public void preventDrawingVanillaBars(RenderGameOverlayEvent.Pre event) {
		if (PersonalConfig.OLD_SCHOOL_GUI) return;
		if (preventDrawing.contains(event.getType())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void hudDraw(RenderGameOverlayEvent.Post event) {

		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP thePlayer = mc.thePlayer;
		if (thePlayer == null || thePlayer.capabilities.isCreativeMode) return;
		GuiIngameForge hud = (GuiIngameForge) mc.ingameGUI;
		ScaledResolution resolution = event.getResolution();

		if (PersonalConfig.OLD_SCHOOL_GUI) {
			if (!MineSouls.isMineSoulsOnServer()) return;

			GlStateManager.enableBlend();
			GlStateManager.pushMatrix();
			{
				GlStateManager.scale(PersonalConfig.GUI_SCALE, PersonalConfig.GUI_SCALE, 1);

				mc.getTextureManager().bindTexture(GUI_TEXTURE);

				int x = 1 + 5;
				int y = 1 + 5;
				for (ScrollBar scrollBar : scrollBars) {
					if (scrollBar.type == null && scrollBar.shouldRender) {
						scrollBar.render(hud, x, y);
						y += scrollBar.TEX_HEIGHT + 1;
					}
				}
				GlStateManager.color(1, 1, 1);
			}
			GlStateManager.popMatrix();
			GlStateManager.disableBlend();

		} else {
			boolean guiLeftSide = PersonalConfig.GUI_LEFT_SIDE;

			GlStateManager.enableBlend();
			GlStateManager.pushMatrix();
			{
				GlStateManager.scale(PersonalConfig.GUI_SCALE, PersonalConfig.GUI_SCALE, 1);

				renderExperience(mc, thePlayer, hud, BORDER, BORDER);

				mc.getTextureManager().bindTexture(GUI_TEXTURE);

				int x = 23 + 2 + BORDER;
				int y = 1 + BORDER;
				for (ScrollBar scrollBar : scrollBars) {
					if (scrollBar.shouldRender) {
						scrollBar.render(hud, x, y);
						y += scrollBar.TEX_HEIGHT + 1;
					}
				}
				GlStateManager.color(1, 1, 1);
			}
			GlStateManager.popMatrix();
			GlStateManager.disableBlend();
		}
	}

	public void renderExperience(Minecraft mc, EntityPlayerSP thePlayer, GuiIngameForge hud, int base_x, int base_y) {
		mc.getTextureManager().bindTexture(GUI_TEXTURE);
		hud.drawTexturedModalRect(base_x, base_y, 0, 0, 23, 23);

		float t = thePlayer.experience;
		if (t > 0) {
			if (t > 1) t = 1;


			double angle = Math.PI / 2 + t * Math.PI * 2;
			double dx = (float) Math.cos(angle);
			double dy = (float) Math.sin(angle);

			double adx = Math.abs(dx);
			double ady = Math.abs(dy);

			if (adx < ady) {
				dx = dx / ady;
				dy = Math.signum(dy);
			} else {
				dy = dy / adx;
				dx = Math.signum(dx);
			}
			double FULL = 23;
			double HALF = FULL / 2;
			dx = (1 + dx) / 2 * FULL;
			dy = FULL - ((1 + dy) / 2 * FULL);

			float zLevel = hud.zLevel;

			Tessellator instance = Tessellator.getInstance();
			VertexBuffer tess = instance.getBuffer();
			tess.begin(7, DefaultVertexFormats.POSITION_TEX);

			if (t <= 0.125) {
				addVertexAndTex(tess, HALF, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, dx, dy, zLevel, base_x, base_y);
				addVertexAndTex(tess, dx, dy, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
			} else if (t <= 0.375) {
				addVertexAndTex(tess, dx, dy, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, 0, 0, zLevel, base_x, base_y);
			} else if (t <= 0.625) {
				addVertexAndTex(tess, 0, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, 0, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, dx, dy, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
				addVertexAndTex(tess, 0, FULL, zLevel, base_x, base_y);
			} else if (t <= 0.875) {
				addVertexAndTex(tess, 0, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, 0, 0, zLevel, base_x, base_y);

				addVertexAndTex(tess, 0, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, FULL, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, dx, dy, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
			} else if (t < 1) {
				addVertexAndTex(tess, 0, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, 0, 0, zLevel, base_x, base_y);

				addVertexAndTex(tess, 0, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, FULL, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, FULL, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);

				addVertexAndTex(tess, FULL, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, dx, dy, zLevel, base_x, base_y);
				addVertexAndTex(tess, dx, dy, zLevel, base_x, base_y);
				addVertexAndTex(tess, HALF, HALF, zLevel, base_x, base_y);
			} else {
				addVertexAndTex(tess, 0, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, FULL, FULL, zLevel, base_x, base_y);
				addVertexAndTex(tess, FULL, 0, zLevel, base_x, base_y);
				addVertexAndTex(tess, 0, 0, zLevel, base_x, base_y);
			}

			instance.draw();
		}

		String text = "" + mc.thePlayer.experienceLevel;
//		if(MineSouls.deobf_folder) {
//			text ="MS";
//		}
		FontRenderer fontrenderer = mc.fontRendererObj;
		int x = base_x + (24 - fontrenderer.getStringWidth(text)) / 2;
		int y = base_y + (26 - fontrenderer.FONT_HEIGHT) / 2;
		fontrenderer.drawString(text, x + 1, y, 0);
		fontrenderer.drawString(text, x - 1, y, 0);
		fontrenderer.drawString(text, x, y + 1, 0);
		fontrenderer.drawString(text, x, y - 1, 0);
		fontrenderer.drawString(text, x, y, 8453920);
	}

	private void addVertexAndTex(VertexBuffer tess, double dx, double dy, float zLevel, int base_x, int base_y) {
		tess.pos(base_x + dx, base_y + dy, zLevel);
		tess.tex((23 + dx) / 256F, dy / 256F);
		tess.endVertex();
	}

	public abstract class ScrollBar {
		final int TEX_LEFT_MAIN_LEFT;
		final int TEX_LEFT_BAR;
		final int TEX_LEFT_MAIN_RIGHT;
		final int TEX_TOP;

		final int TEX_WIDTH;
		final int TEX_HEIGHT;
		final float r, g, b;
		final RenderGameOverlayEvent.ElementType type;
		final int base_max;
		public int yellow_r = 1;
		public int yellow_g = 1;
		public int yellow_b = 0;
		float value = -1;
		float prevValue = -1;
		float yellowValue = -1;
		float prevYellowValue = -1;
		boolean shouldRender = false;
		int barWidth;
		boolean emergency = false;

		protected ScrollBar(int base_max, RenderGameOverlayEvent.ElementType type, float g, float b, float r) {
			this(46, 0, 16, 5, r, g, b, type, base_max);
		}

		protected ScrollBar(int tex_left_main_left, int tex_top, int tex_width, int tex_height, float r, float g, float b, RenderGameOverlayEvent.ElementType type, int base_max) {
			this(tex_left_main_left, tex_left_main_left + tex_width, tex_left_main_left + tex_width * 2, tex_top, tex_width, tex_height, r, g, b, type, base_max);
		}

		protected ScrollBar(int tex_left_main_left, int tex_left_bar, int tex_left_main_right, int tex_top, int tex_width, int tex_height, float r, float g, float b, RenderGameOverlayEvent.ElementType type, int base_max) {
			TEX_LEFT_MAIN_LEFT = tex_left_main_left;
			TEX_LEFT_BAR = tex_left_bar;
			TEX_LEFT_MAIN_RIGHT = tex_left_main_right;
			TEX_TOP = tex_top;
			TEX_WIDTH = tex_width;
			TEX_HEIGHT = tex_height;
			this.r = r;
			this.g = g;
			this.b = b;
			this.type = type;
			this.base_max = base_max;
		}

		public abstract float getCurrentValue(EntityPlayer player, PlayerHandler handler);

		public float getMaxValue(EntityPlayer player, PlayerHandler handler) {
			return base_max;
		}

		public boolean shouldRender(EntityPlayer player, PlayerHandler handler) {
			return true;
		}

		public int getWidth() {
			return this.barWidth + 1;
		}

		public void render(GuiIngameForge hud, int x, int y) {
			int barWidth = this.barWidth;
			int renderValue = (int) Math.ceil((prevValue + partialTime * (value - prevValue)) * barWidth);
			int yellowRenderValue = (int) Math.ceil((prevYellowValue + partialTime * (yellowValue - prevYellowValue)) * barWidth);

			// Draw Bar Border
			GlStateManager.color(1, 1, 1);
			int dx;
			int dw;

			for (dx = 0; dx < barWidth; dx += dw) {
				dw = Math.min(dx + TEX_WIDTH, barWidth) - dx;
				hud.drawTexturedModalRect(x + dx, y, TEX_LEFT_MAIN_LEFT, TEX_TOP, dw, TEX_HEIGHT);
			}
			int cap_width = barWidth + 1 - dx;
			hud.drawTexturedModalRect(x + dx, y, TEX_LEFT_MAIN_RIGHT + TEX_WIDTH - cap_width, TEX_TOP, cap_width, TEX_HEIGHT);


			// Draw Yellow Bar
			if (yellowRenderValue > renderValue) {
				GlStateManager.color(yellow_r, yellow_g, yellow_b);
				for (dx = 0; dx < yellowRenderValue; dx += TEX_WIDTH) {
					if (dx + TEX_WIDTH < renderValue)
						continue; // will be rendered by the base bar

					int ax = dx < renderValue ? renderValue : dx;
					int w = Math.min(dx + TEX_WIDTH, yellowRenderValue) - ax;
					hud.drawTexturedModalRect(x + ax, y, TEX_LEFT_BAR + (ax - dx), TEX_TOP, w, TEX_HEIGHT);
				}
			}

			// Draw Bar
			GlStateManager.color(r, g, b);
			for (dx = 0; dx < renderValue; dx += TEX_WIDTH) {
				int w = dx + TEX_WIDTH > renderValue ? (renderValue - dx) : TEX_WIDTH;
				hud.drawTexturedModalRect(x + dx, y, TEX_LEFT_BAR, TEX_TOP, w, TEX_HEIGHT);
			}

			if (emergency) {
				GlStateManager.color(1, 1, 1, intensity);
				for (dx = 0; dx < barWidth; dx += dw) {
					dw = Math.min(dx + TEX_WIDTH, barWidth) - dx;
					hud.drawTexturedModalRect(x + dx, y, TEX_LEFT_MAIN_LEFT, TEX_TOP + TEX_HEIGHT, dw, TEX_HEIGHT);
				}
				cap_width = barWidth + 1 - dx;
				hud.drawTexturedModalRect(x + dx, y, TEX_LEFT_MAIN_RIGHT + TEX_WIDTH - cap_width, TEX_TOP + TEX_HEIGHT, cap_width, TEX_HEIGHT);
			}
		}

		public void clientTick() {
			Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
			if (!(renderViewEntity instanceof EntityPlayer)) return;
			EntityPlayerSP thePlayer = (EntityPlayerSP) renderViewEntity;

			PlayerHandler handler = PlayerHandlerRegistry.INSTANCE.getPlayerHandler(thePlayer);

			prevValue = value;
			prevYellowValue = yellowValue;
			shouldRender = shouldRender(thePlayer, handler);

			emergency = isEmergency(thePlayer, handler);

			if (!shouldRender) {
				prevValue = -1;
				value = -1;
				prevYellowValue = -1;
				yellowValue = -1;
				return;
			}

			float currentValue = getCurrentValue(thePlayer, handler);
			float maxValue = getMaxValue(thePlayer, handler);

			float nextValue;
			if (maxValue <= currentValue || maxValue <= 0) {
				nextValue = 1;
			} else {
				nextValue = Math.max(currentValue / maxValue, 0);
			}

			barWidth = (int) (maxValue / base_max * 80F + 0.5F);

			if (value == -1) {
				yellowValue = nextValue;
				value = nextValue;
			} else {
				value = nextValue * 0.4F + value * 0.6F;
				yellowValue = Math.max(value, yellowValue - 1 / 100F);
			}


			if (prevValue == -1) {
				prevValue = value;
				prevYellowValue = yellowValue;
			}
		}

		protected abstract boolean isEmergency(EntityPlayerSP thePlayer, PlayerHandler handler);
	}
}
