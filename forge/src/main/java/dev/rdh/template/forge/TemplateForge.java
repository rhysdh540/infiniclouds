package dev.rdh.template.forge;

import dev.rdh.template.Template;

import net.minecraftforge.fml.common.Mod;

@Mod(Template.ID)
public class TemplateForge {
	public TemplateForge() {
		Template.init();
	}
}