package dev.rdh.infiniclouds.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	private int lastViewDistance;
	
	private static final float OFFSET = 1f / 256f;

	@Shadow
	private boolean generateClouds;
	
	@Shadow
	@Nullable
	private VertexBuffer cloudBuffer;

	@Shadow
	protected abstract RenderedBuffer buildClouds(BufferBuilder builder, double x, double y, double z, Vec3 color);

	@Shadow
	@Nullable
	private ClientLevel level;

	@Shadow
	private int ticks;

	@Shadow
	private int prevCloudX;

	@Shadow
	private int prevCloudY;

	@Shadow
	private int prevCloudZ;

	@Shadow
	private Vec3 prevCloudColor;

	@Shadow @Final
	private Minecraft minecraft;

	@Shadow @Final
	private static ResourceLocation CLOUDS_LOCATION;

	@Shadow @Nullable
	private CloudStatus prevCloudsType;

	private float oldFogEnd = 0;

	// Async clouds
	@Nullable
	private Future<RenderedBuffer> cloudBuildTask = null;
	private final BufferBuilder cloudBufferBuilder = new BufferBuilder(1000);
	private final ExecutorService cloudMesher = Executors.newSingleThreadExecutor(r -> new Thread(r, "CloudMesher"));
	private static final float CLOUD_SIZE = 12.0F;
	private int cloudsMeshX = 0;
	private int cloudsMeshY = 0;
	private int cloudsMeshZ = 0;
	private int cloudMeshTargetX = 0;
	private int cloudMeshTargetY = 0;
	private int cloudMeshTargetZ = 0;



	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void buildClouds(PoseStack poseStack, Matrix4f projectionMatrix, float tickDelta, double xOffset, double yOffset, double zOffset) {
		float cloudHeight = this.level.effects().getCloudHeight();
		if (!Float.isNaN(cloudHeight)) {
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.depthMask(true);

			// calculate position
			double tickOffset = ((float)this.ticks + tickDelta) * 0.03F;
			double x = (xOffset + tickOffset) / CLOUD_SIZE;
			double y = cloudHeight - (float)yOffset + 0.33F;
			double z = zOffset / CLOUD_SIZE + 0.33000001311302185;

			// calculate mesh position
			float fracX = (float)(x - (double) Mth.floor(x));
			float fracY = (float)(y / 4.0 - (double)Mth.floor(y / 4.0)) * 4.0F;
			float fracZ = (float)(z - (double)Mth.floor(z));
			Vec3 color = this.level.getCloudColor(tickDelta);

			// check if clouds should be remeshed
			int posX = (int)Math.floor(x);
			int posY = (int)Math.floor(y / 4.0);
			int posZ = (int)Math.floor(z);
			if (posX != this.prevCloudX || posY != this.prevCloudY || posZ != this.prevCloudZ || this.minecraft.options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(color) > 2.0E-4) {
				this.prevCloudX = posX;
				this.prevCloudY = posY;
				this.prevCloudZ = posZ;
				this.prevCloudColor = color;
				this.prevCloudsType = this.minecraft.options.getCloudsType();
				this.generateClouds = true;
			}

			if (this.cloudBuildTask != null) {
				if (cloudBuildTask.isDone()) {
					try {
						if (this.cloudBuffer != null) {
							this.cloudBuffer.close();
						}

						this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
						RenderedBuffer builtBuffer = cloudBuildTask.get();
						this.cloudBuffer.bind();
						this.cloudBuffer.upload(builtBuffer);
						VertexBuffer.unbind();

						this.cloudsMeshX = this.cloudMeshTargetX;
						this.cloudsMeshY = this.cloudMeshTargetY;
						this.cloudsMeshZ = this.cloudMeshTargetZ;
						this.cloudBuildTask = null;
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				if (this.generateClouds) {
					this.cloudMeshTargetX = posX;
					this.cloudMeshTargetY = posY;
					this.cloudMeshTargetZ = posZ;
					this.cloudBuildTask = this.cloudMesher.submit(() -> {
						this.cloudBufferBuilder.clear();
						return this.buildClouds(this.cloudBufferBuilder, x, y, z, color);
					});
					this.generateClouds = false;
				}
			}


			RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
			RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
			FogRenderer.levelFogColor();
			poseStack.pushPose();
			poseStack.scale(12.0F, 1.0F, 12.0F);
			poseStack.translate((-fracX) + (this.cloudsMeshX - posX), fracY - ((this.cloudsMeshY - posY) * 4.0), (-fracZ) + (this.cloudsMeshZ - posZ));
			if (this.cloudBuffer != null) {
				this.cloudBuffer.bind();
				int l = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

				for(int i1 = l; i1 < 2; ++i1) {
					if (i1 == 0) {
						RenderSystem.colorMask(false, false, false, false);
					} else {
						RenderSystem.colorMask(true, true, true, true);
					}

					ShaderInstance shaderinstance = RenderSystem.getShader();
					this.cloudBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shaderinstance);
				}

				VertexBuffer.unbind();
			}

			poseStack.popPose();
			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	}

	@Inject(
			method = "renderClouds",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"
			)
	)
	private void fixFoxStart(PoseStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f, CallbackInfo ci) {
		oldFogEnd = RenderSystem.getShaderFogEnd();
		RenderSystem.setShaderFogEnd(oldFogEnd * 4f);
	}

	@Inject(
			method = "buildClouds",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;pop()V"
			)
	)
	private void fixFogEnd(PoseStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f, CallbackInfo ci) {
		RenderSystem.setShaderFogEnd(oldFogEnd);
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = -3)
	)
	private int fancyForStart(int constant) {
		return -(viewDistanceModified() - 1);
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = 4)
	)
	private int fancyForEnd(int constant) {
		return viewDistanceModified();
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = -32)
	)
	private int fastForStart(int constant) {
		return -(viewDistanceModified() * 4);
	}

	@ModifyConstant(
			method = "buildClouds",
			constant = @Constant(intValue = 32)
	)
	private int fastForEnd(int constant) {
		return (viewDistanceModified() * 4);
	}

	private int viewDistanceModified() {
		return lastViewDistance * 4;
	}
}
