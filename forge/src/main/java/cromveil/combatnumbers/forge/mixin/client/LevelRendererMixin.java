package cromveil.combatnumbers.forge.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.BufferSourceAdapter;
import cromveil.combatnumbers.client.render.CameraAdapter;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.RenderOption;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Inject(method = "renderLevel", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender"
					+ "(Lnet/minecraft/client/Camera;"
					+ "Lnet/minecraft/client/renderer/culling/Frustum;"
					+ "ZZ)V"))
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

		double gameTime = mc.level.getGameTime()
				+ mc.getTimer().getGameTimeDeltaPartialTick(false);
		ctx.tickTexts(gameTime);

		if (!ctx.shouldRenderWorld()) {
			return;
		}

		RenderOption option = ctx.renderOption();

		PoseStack poseStack = new PoseStack();
		FloatingTextRenderer.renderAll(
				BillboardStrategy.create(option, poseStack,
						new BufferSourceAdapter(mc.renderBuffers().bufferSource()),
						CameraAdapter.from(mc.gameRenderer.getMainCamera())),
				ctx.config(), ctx.textManager());
	}
}
