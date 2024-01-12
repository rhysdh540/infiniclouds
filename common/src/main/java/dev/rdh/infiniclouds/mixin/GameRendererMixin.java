package dev.rdh.infiniclouds.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "getDepthFar", at = @At("RETURN"), cancellable = true)
	private void extendFarPlaneDistance(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(1024f);
	}
}
