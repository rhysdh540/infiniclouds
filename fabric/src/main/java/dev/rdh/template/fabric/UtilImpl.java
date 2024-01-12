package dev.rdh.template.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class UtilImpl {
	public static String getVersion(String modid) {
		return FabricLoader.getInstance()
				.getModContainer(modid)
				.orElseThrow()
				.getMetadata()
				.getVersion()
				.getFriendlyString();
	}

	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static String platformName() {
		return FabricLoader.getInstance().isModLoaded("quilt_loader") ? "Quilt" : "Fabric";
	}
}
