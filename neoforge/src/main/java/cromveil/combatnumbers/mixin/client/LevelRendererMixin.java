package cromveil.combatnumbers.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.Level;
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
			+ "Lorg/joml/Matrix4f;"
			+ "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
			+ "Z"
			+ "Lnet/minecraft/client/renderer/state/LevelRenderState;"
			+ "Lnet/minecraft/client/DeltaTracker;"
			+ "Lnet/minecraft/util/profiling/ProfilerFiller;"
			+ ")V"))
	private void injectCombatNumbersRender(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (!Config.get(ConfigIds.CLIENT_ENABLED)) {
			FloatingTextManager.clear();
			return;
		}

		Level level = mc.level;
		if (level == null) {
			FloatingTextManager.clear();
			return;
		}

		double gameTime = level.getGameTime() + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		for (FloatingText text : FloatingTextManager.getActive()) {
			text.setGameTime(gameTime);
		}
		FloatingTextManager.cleanupExpired();

		RenderOption option = Config.get(ConfigIds.RENDER_MODE);
		if (option.isHud()) {
			return;
		}

		CameraRenderState cam = mc.gameRenderer.getLevelRenderState().cameraRenderState;
		PoseStack poseStack = new PoseStack();
		FloatingTextRenderer.renderAll(
				BillboardStrategy.create(option, poseStack, submitNodeStorage, cam));
	}
}
