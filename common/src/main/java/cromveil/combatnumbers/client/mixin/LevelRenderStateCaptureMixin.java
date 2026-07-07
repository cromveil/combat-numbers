package cromveil.combatnumbers.client.mixin;

import cromveil.combatnumbers.client.render.RenderStateCache;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the view-rotation and projection matrices from
 * {@code LevelRenderer.renderLevel()} so that {@link RenderStateCache}
 * and {@code CameraAdapter} can construct a {@code RenderCamera}
 * without a {@code CameraRenderState} (which does not exist in 1.21.1).
 */
@Mixin(LevelRenderer.class)
public class LevelRenderStateCaptureMixin {

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void captureLevelRenderState(
			DeltaTracker deltaTracker,
			boolean renderBlockOutline,
			Camera camera,
			GameRenderer gameRenderer,
			LightTexture lightTexture,
			Matrix4f viewRotation,
			Matrix4f projection, // < 1.21.11: viewRotation and projection args are swapped
			CallbackInfo ci) {
		RenderStateCache.capture(projection, viewRotation, camera.getPosition());
	}
}
