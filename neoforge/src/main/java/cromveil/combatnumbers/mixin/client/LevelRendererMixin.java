package cromveil.combatnumbers.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.config.ModConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// FIXME: probably needs to be moved to a custom render pipeline
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Shadow
	private SubmitNodeStorage submitNodeStorage;

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;submitFeatures(" +
					"Lnet/minecraft/client/renderer/state/level/LevelRenderState;" +
					"Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V"
		)
	)
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
		if (!ModConfig.getInstance().enabled) {
			FloatingTextManager.clear();
			return;
		}

		Level level = mc.level;
		if (level == null) {
			FloatingTextManager.clear();
			return;
		}

		double gameTime = level.getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false);
		for (FloatingText text : FloatingTextManager.getActive()) {
			text.setGameTime(gameTime);
		}
		FloatingTextManager.cleanupExpired();

		PoseStack poseStack = new PoseStack();
		FloatingTextRenderer.renderAll(poseStack, submitNodeStorage, cameraState);
	}
}
