package cromveil.combatnumbers.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.platform.Services;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Shadow
	private SubmitNodeStorage submitNodeStorage;

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass("
			+ "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;"
			+ "Lnet/minecraft/client/renderer/culling/Frustum;"
			+ "Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
			+ "ZLnet/minecraft/client/renderer/state/level/LevelRenderState;"
			+ "Lnet/minecraft/client/DeltaTracker;"
			+ "Lnet/minecraft/util/profiling/ProfilerFiller;"
			+ "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;"
			+ ")V"))
	private void injectCombatNumbersRender(
			GraphicsResourceAllocator resourceAllocator,
			DeltaTracker deltaTracker,
			boolean renderOutline,
			CameraRenderState cameraState,
			Matrix4fc modelViewMatrix,
			GpuBufferSlice terrainFog,
			Vector4f fogColor,
			boolean shouldRenderSky,
			ChunkSectionsToRender chunkSectionsToRender,
			CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (!Services.CONFIG.clientEnabled()) {
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

		RenderOption option = Services.CONFIG.renderMode();
		if (option.isHud()) {
			return;
		}

		PoseStack poseStack = new PoseStack();
		FloatingTextRenderer.renderAll(
				BillboardStrategy.create(option, poseStack, submitNodeStorage, cameraState));
	}
}
