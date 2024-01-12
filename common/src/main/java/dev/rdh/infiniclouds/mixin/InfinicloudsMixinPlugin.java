package dev.rdh.infiniclouds.mixin;

import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import dev.rdh.infiniclouds.Util;

import java.util.List;
import java.util.Set;

public class InfinicloudsMixinPlugin implements IMixinConfigPlugin {
	private boolean sodiumLoaded = false;

	@Override
	public void onLoad(String mixinPackage) {
		if(Util.isModLoaded("sodium")) {
			sodiumLoaded = true;
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if(sodiumLoaded && targetClassName.equals("net.minecraft.client.renderer.LevelRenderer")) {
			return false;
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
