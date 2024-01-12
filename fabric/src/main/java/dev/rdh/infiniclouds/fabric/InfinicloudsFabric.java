package dev.rdh.infiniclouds.fabric;

import dev.rdh.infiniclouds.Infiniclouds;

import net.fabricmc.api.ModInitializer;

public class InfinicloudsFabric extends Infiniclouds implements ModInitializer {
	@Override
	public void onInitialize() {
		init();
	}
}