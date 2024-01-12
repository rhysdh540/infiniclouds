package dev.rdh.infiniclouds.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	private int lastViewDistance;

	private float oldFogEnd = 0;

	@Inject(
			method = "renderClouds",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"
			)
	)
	private void fixFogStart(PoseStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f, CallbackInfo ci) {
		oldFogEnd = RenderSystem.getShaderFogEnd();
		RenderSystem.setShaderFogEnd(oldFogEnd * 4);
	}

	@Inject(
			method = "renderClouds",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"
			)
	)
	private void fixFogEnd(PoseStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f, CallbackInfo ci) {
		RenderSystem.setShaderFogEnd(oldFogEnd);
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = -3)
	)
	private int fancyFogStart(int constant) {
		return -(viewDistanceModified() - 1);
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = 4)
	)
	private int fancyFogEnd(int constant) {
		return viewDistanceModified();
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = -32)
	)
	private int fastFogStart(int constant) {
		return -(viewDistanceModified() * 4);
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = 32)
	)
	private int fastFogEnd(int constant) {
		return (viewDistanceModified() * 4);
	}

	private int viewDistanceModified() {
		return 512;
	}
}
