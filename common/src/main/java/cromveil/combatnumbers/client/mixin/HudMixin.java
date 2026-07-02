package cromveil.combatnumbers.client.mixin;

import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.HudStrategy;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HudMixin {

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void combatnumbers$renderFloatingTextHud(
			GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!Config.get(ConfigIds.ENABLED) || !Config.get(ConfigIds.RENDER_MODE).isHud()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.options.hideGui) {
			return;
		}

		CameraRenderState cam = mc.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
		FloatingTextRenderer.renderAll(new HudStrategy(graphics, cam));
	}
}
