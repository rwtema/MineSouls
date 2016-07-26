package com.rwtema.minesouls.config;

import com.rwtema.minesouls.MineSouls;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class SoulsConfigGui extends GuiConfig {
	public SoulsConfigGui(GuiScreen parentScreen) {
		super(parentScreen,
				createConfigElements(),
				MineSouls.MODID,
				"Debug",
				false,
				false,
				"MineSoulsDebug", null);
	}

	private static List<IConfigElement> createConfigElements() {
		Configuration configuration = ConfigHandler.configuration;

		List<IConfigElement> list = new ArrayList<>();
		for (String category : ConfigHandler.configurableCategories) {
			list.addAll(new ConfigElement(configuration.getCategory(category)).getChildElements());
		}

		return list;
	}


}
