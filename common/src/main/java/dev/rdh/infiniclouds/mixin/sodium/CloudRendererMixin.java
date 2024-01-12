package dev.rdh.infiniclouds.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.sodium.client.render.immediate.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CloudRenderer.class)
public class CloudRendererMixin {
	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0))
	private int extendCloudDistance(int original) {
		return 512;
	}
}
