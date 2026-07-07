package cromveil.combatnumbers.client.mixin;

import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.render.CameraAdapter;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.GuiGraphicsAdapter;
import cromveil.combatnumbers.client.render.HudStrategy;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HudMixin {

	@Inject(method = "render", at = @At("TAIL"))
	private void combatnumbers$renderFloatingTextHud(
			GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		RenderContext ctx = MixinBridge.CONTEXT;
		if (ctx == null || !ctx.shouldRenderHud()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.options.hideGui) {
			return;
		}

		var cam = CameraAdapter.from(mc.gameRenderer.getLevelRenderState().cameraRenderState);
		var gfx = new GuiGraphicsAdapter(graphics);
		FloatingTextRenderer.renderAll(new HudStrategy(gfx, cam), ctx.config(), ctx.textManager());
	}
}
