package dev.rdh.infiniclouds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Infiniclouds {
	public static final String ID = "infiniclouds";
	public static final String NAME = "Infiniclouds";
	public static final String VERSION = Util.getVersion(ID).split("-build")[0];
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static void init() {
		LOGGER.info("{} v{} initializing! on platform: {}",
				NAME, VERSION, Util.platformName());
	}
}
