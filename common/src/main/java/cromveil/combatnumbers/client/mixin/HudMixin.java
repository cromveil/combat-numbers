package cromveil.combatnumbers.client.mixin;

import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.render.CameraAdapter;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.GuiGraphicsExtractorAdapter;
import cromveil.combatnumbers.client.render.HudStrategy;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
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
		RenderContext ctx = MixinBridge.CONTEXT;
		if (ctx == null || !ctx.shouldRenderHud()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || this.isHidden()) {
			return;
		}

		var cam = CameraAdapter.from(mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState);
		var gfx = new GuiGraphicsExtractorAdapter(graphics);
		FloatingTextRenderer.renderAll(new HudStrategy(gfx, cam), ctx.config(), ctx.textManager());
	}
}
