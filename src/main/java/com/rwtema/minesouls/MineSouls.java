package com.rwtema.minesouls;

import com.google.common.base.Throwables;
import com.rwtema.minesouls.compat.TCCompat;
import com.rwtema.minesouls.config.ConfigHandler;
import com.rwtema.minesouls.config.DifficultyConfig;
import com.rwtema.minesouls.config.PersonalConfig;
import com.rwtema.minesouls.coremod.MineSoulsControlsCoreMod;
import com.rwtema.minesouls.network.NetworkHandler;
import com.rwtema.minesouls.playerHandler.PlayerHandlerRegistry;
import java.net.URL;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = MineSouls.MODID, version = MineSouls.VERSION, guiFactory = "com.rwtema.minesouls.config.SoulsConfigGuiFactory", acceptedMinecraftVersions = "[1.9.4,1.10)")
public class MineSouls {
	public static final String MODID = "minesouls";
	public static final String VERSION = "1.0";

	public static final boolean deobf;
	public static final boolean deobf_folder;

	@SidedProxy(serverSide = "com.rwtema.minesouls.Proxy", clientSide = "com.rwtema.minesouls.ProxyClient")
	public static Proxy proxy;

	private static boolean mineSoulsOnServer = true;

	static {
		boolean d;
		try {
			net.minecraft.world.World.class.getMethod("getBlockState", BlockPos.class);
			d = true;
		} catch (NoSuchMethodException | SecurityException e) {
			d = false;
		}
		deobf = d;


		if (deobf) {
			URL resource = MineSouls.class.getClassLoader().getResource(MineSouls.class.getName().replace('.', '/').concat(".class"));
			deobf_folder = resource != null && "file".equals(resource.getProtocol());
		} else
			deobf_folder = false;

		if (!MineSoulsControlsCoreMod.loaded) {
			String message = "MineSouls CoreMod Failed To Load";
			if (deobf_folder)
				message = message + "\nAdd to VM Options:  \"-Dfml.coreMods.load=" + MineSoulsControlsCoreMod.class.getName() + "\"";

			throw new LoaderException(message);
		}
	}

	public static boolean isMineSoulsOnServer() {
		return proxy.isServer() || mineSoulsOnServer;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.addClass(DifficultyConfig.class, deobf_folder);
		ConfigHandler.addClass(PersonalConfig.class, true);
		ConfigHandler.buildConfig(event.getSuggestedConfigurationFile());
		PlayerHandlerRegistry.init();
		NetworkHandler.init();
		proxy.onLoad();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("tconstruct")) {
			((Runnable) (TCCompat::init)).run();
		}
	}

	@NetworkCheckHandler
	public boolean check(Map<String, String> remoteVersions, Side side) {
		if (remoteVersions.containsKey(MODID)) {
			ModContainer containerFor = FMLCommonHandler.instance().findContainerFor(this);
			boolean b = containerFor.getVersion().equals(remoteVersions.get(MODID));
			if (side == Side.SERVER)
				mineSoulsOnServer = b;
			return b;
		} else {
			if (side == Side.SERVER) {
				mineSoulsOnServer = false;
			}
			return side == Side.SERVER;
		}
	}

	@Mod.EventHandler
	public void onServerStart(FMLServerAboutToStartEvent event) {
		mineSoulsOnServer = true;
		try {
			ConfigHandler.reloadPropertiesFromConfig();
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}
}
