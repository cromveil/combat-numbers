package cromveil.combatnumbers.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;

import cromveil.combatnumbers.client.render.RenderStateCache;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1 onwards we get access to precomputed projection matrix with the FOV we need from CameraRenderState
 * It's not there in 1.21.11 so now we have this hacky workaround to grab the stuff we need
 */
@Mixin(LevelRenderer.class)
public class LevelRenderStateCaptureMixin {

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void captureLevelRenderState(
			GraphicsResourceAllocator graphicsResourceAllocator,
			DeltaTracker deltaTracker,
			boolean bl,
			Camera camera,
			Matrix4f matrix4f,
			Matrix4f matrix4f2,
			Matrix4f matrix4f3,
			GpuBufferSlice gpuBufferSlice,
			Vector4f vector4f,
			boolean bl2,
			CallbackInfo ci) {
		RenderStateCache.capture(
				matrix4f3, matrix4f, camera.position());
	}
}
