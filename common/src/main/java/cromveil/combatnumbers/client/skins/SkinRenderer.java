package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.client.render.GeometrySubmitter;
import cromveil.combatnumbers.client.render.HudRenderContext;

public interface SkinRenderer {
	void render3d(PoseStack poseStack, GeometrySubmitter geom, float alpha, int light);

	default void renderChar3d(int index, PoseStack poseStack, GeometrySubmitter geom,
			float alpha, int light) {
		render3d(poseStack, geom, alpha, light);
	}

	default void render2d(HudRenderContext ctx, float alpha) {
	}

	default void renderChar2d(int index, HudRenderContext ctx, float alpha) {
		render2d(ctx, alpha);
	}
}
