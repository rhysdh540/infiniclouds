package dev.rdh.infiniclouds;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class Util {
	@ExpectPlatform
	public static String getVersion(String modid) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static String platformName() {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean isModLoaded(String modid) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean isDevEnv() {
		throw new AssertionError();
	}
}
