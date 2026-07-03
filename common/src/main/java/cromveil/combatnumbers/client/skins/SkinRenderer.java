package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.client.render.HudRenderContext;
import net.minecraft.client.renderer.SubmitNodeCollector;

public interface SkinRenderer {
	void render3d(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float alpha, int light);

	default void renderChar3d(int index, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			float alpha, int light) {
		render3d(poseStack, submitNodeCollector, alpha, light);
	}

	default void render2d(HudRenderContext ctx, float alpha) {
	}

	default void renderChar2d(int index, HudRenderContext ctx, float alpha) {
		render2d(ctx, alpha);
	}
}
