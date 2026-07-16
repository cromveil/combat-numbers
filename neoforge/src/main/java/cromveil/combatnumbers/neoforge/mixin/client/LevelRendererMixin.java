package cromveil.combatnumbers.neoforge.mixin.client;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.CameraAdapter;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.client.render.SubmitNodeCollectorAdapter;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Shadow
	private SubmitNodeStorage submitNodeStorage;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;submitFeatures("
			+
			"Lnet/minecraft/client/renderer/state/level/LevelRenderState;" +
			"Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V"))
	private void injectCombatNumbersRender(
			GraphicsResourceAllocator resourceAllocator,
			DeltaTracker deltaTracker,
			boolean renderOutline,
			CameraRenderState cameraState,
			Matrix4fc modelViewMatrix,
			GpuBufferSlice terrainFog,
			Vector4f fogColor,
			boolean shouldRenderSky,
			CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		RenderContext ctx = MixinBridge.CONTEXT;

		if (mc.level == null) {
			if (ctx != null)
				ctx.textManager().clear();
			return;
		}

		if (ctx == null)
			return;

		double gameTime = mc.level.getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false);
		ctx.tickTexts(gameTime);

		if (!ctx.shouldRenderWorld()) {
			return;
		}

		RenderOption option = ctx.renderOption();

		PoseStack poseStack = new PoseStack();
		FloatingTextRenderer.renderAll(
				BillboardStrategy.create(option, poseStack,
						new SubmitNodeCollectorAdapter(submitNodeStorage),
						CameraAdapter.from(cameraState)),
				ctx.config(), ctx.textManager());
	}
}
