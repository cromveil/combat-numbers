package cromveil.combatnumbers.forge.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.CameraRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		RenderContext ctx = MixinBridge.CONTEXT;

		if (mc.level == null) {
			if (ctx != null)
				ctx.textManager().clear();
			return;
		}

		if (ctx == null)
			return;

		double gameTime = mc.level.getGameTime() + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		ctx.tickTexts(gameTime);

		if (!ctx.shouldRenderWorld()) {
			return;
		}

		RenderOption option = ctx.renderOption();

		CameraRenderState cam = mc.gameRenderer.getLevelRenderState().cameraRenderState;
		PoseStack poseStack = new PoseStack();
		FloatingTextRenderer.renderAll(
				BillboardStrategy.create(option, poseStack,
						new SubmitNodeCollectorAdapter(submitNodeStorage),
						CameraAdapter.from(cam)),
				ctx.config(), ctx.textManager());
	}
}