package dev.rdh.template.fabric;

import dev.rdh.template.Template;

import net.fabricmc.api.ModInitializer;

public class TemplateFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Template.init();
	}
}