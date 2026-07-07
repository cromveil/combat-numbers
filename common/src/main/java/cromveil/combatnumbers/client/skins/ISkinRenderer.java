package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.client.render.IGeometrySubmitter;
import cromveil.combatnumbers.client.render.IHudRenderContext;

public interface ISkinRenderer {
	void render3d(PoseStack poseStack, IGeometrySubmitter geom, float alpha, int light);

	default void renderChar3d(int index, PoseStack poseStack, IGeometrySubmitter geom,
			float alpha, int light) {
		render3d(poseStack, geom, alpha, light);
	}

	default void render2d(IHudRenderContext ctx, float alpha) {
	}

	default void renderChar2d(int index, IHudRenderContext ctx, float alpha) {
		render2d(ctx, alpha);
	}
}
