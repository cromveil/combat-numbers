package cromveil.combatnumbers.client.mixin;

import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.HudStrategy;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.config.ModConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {

	@Shadow
	public abstract boolean isHidden();

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void combatnumbers$renderFloatingTextHud(
			GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		var config = ModConfig.getInstance();
		if (!config.enabled || !RenderOption.fromConfig(config.renderMode).isHud()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || this.isHidden()) {
			return;
		}

		CameraRenderState cam = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
		FloatingTextRenderer.renderAll(new HudStrategy(graphics, cam));
	}
}
