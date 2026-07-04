package cromveil.combatnumbers.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.Systems;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.CameraAdapter;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.client.render.SubmitNodeCollectorAdapter;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
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
		if (!Config.get(ConfigIds.ENABLED)) {
			Systems.client().textManager().clear();
			return;
		}

		Level level = mc.level;
		if (level == null) {
			Systems.client().textManager().clear();
			return;
		}

		double gameTime = level.getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false);
		for (FloatingText text : Systems.client().textManager().getActive()) {
			text.setGameTime(gameTime);
		}
		Systems.client().textManager().cleanupExpired();

		RenderOption option = Config.get(ConfigIds.RENDER_MODE);
		if (option.isHud()) {
			return;
		}

		PoseStack poseStack = new PoseStack();
		FloatingTextRenderer.renderAll(
				BillboardStrategy.create(option, poseStack,
						new SubmitNodeCollectorAdapter(submitNodeStorage),
						CameraAdapter.from(cameraState)));
	}
}
