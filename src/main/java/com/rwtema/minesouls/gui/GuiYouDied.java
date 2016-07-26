package com.rwtema.minesouls.gui;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class GuiYouDied extends GuiGameOver {

	public static final int scale = 6;
	public static final int youDiedSize = 9 * scale;
	public static final int bandHeight = youDiedSize;

	public GuiYouDied(@Nullable ITextComponent causeOfDeath) {
		super(causeOfDeath);
	}

	@Override
	public void initGui() {
		super.initGui();
		int buttonY = (this.height / 2 + bandHeight / 2 + this.height) / 2;

		buttonList.get(0).xPosition = this.width / 2 - 10 - 200;
		buttonList.get(0).yPosition = buttonY;

		buttonList.get(1).xPosition = this.width / 2 + 10;
		buttonList.get(1).yPosition = buttonY;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (enableButtonsTimer == 0) return;
		float v = enableButtonsTimer / 20F;
		if (v == 0) return;

		int alpha = 0xff000000;

		if (v < 1) {
			alpha = ((int) MathHelper.ceiling_float_int(v * 255)) << 24;
		}

		boolean flag = this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled();
		int endColor = alpha;
		this.drawGradientRect(0, 0, this.width, this.height / 2 - bandHeight / 2, 0, endColor);
		this.drawGradientRect(0, this.height / 2 - bandHeight / 2, this.width, this.height / 2 + bandHeight / 2, endColor, endColor);
		this.drawGradientRect(0, this.height / 2 + bandHeight / 2, this.width, this.height, endColor, 0);
		GlStateManager.enableBlend();

		GlStateManager.pushMatrix();
		{
			float scale = GuiYouDied.scale;

			GlStateManager.translate(this.width / 2, (this.height / 2 - youDiedSize / 2), 0);
			GlStateManager.scale(scale, scale, scale);

			this.drawCenteredString(
					this.fontRendererObj,
					I18n.format(flag ? "deathScreen.title.hardcore" : "deathScreen.title").replace("!", "").toUpperCase(), 0, 0, 0x8F2B2B | alpha);
		}
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();

		int i = (this.height / 2 - bandHeight / 2) + youDiedSize + 4;
		if (this.causeOfDeath != null) {
			this.drawCenteredString(this.fontRendererObj, this.causeOfDeath.getFormattedText(), this.width / 2, i, 0x8F2B2B | alpha);

			GlStateManager.color(1, 1, 1, 1);

			if (mouseY > i && mouseY < i + this.fontRendererObj.FONT_HEIGHT) {
				ITextComponent itextcomponent = this.getClickedComponentAt(mouseX);

				if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
					this.handleComponentHover(itextcomponent, mouseX, mouseY);
				}
			}
		}

		if (enableButtonsTimer >= 20) {
			for (GuiButton button : this.buttonList) {
				button.drawButton(this.mc, mouseX, mouseY);
			}
		}

		for (GuiLabel label : this.labelList) {
			label.drawLabel(this.mc, mouseX, mouseY);
		}

		GlStateManager.color(1, 1, 1, 1);
	}
}
