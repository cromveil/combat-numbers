package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Fixed view-space depth, was actually easier to implement than world strategy,
 * but still suffers from translucent objects like water being drawn over it.
 */
public final class ScreenStrategy extends BillboardStrategy {

	private static final float FIXED_DEPTH = 1.0f;

	ScreenStrategy(PoseStack ps, SubmitNodeCollector collector, CameraRenderState cam) {
		super(ps, collector, cam);
	}

	@Override
	protected float anchor(Vec3 worldPos) {
		float depth = BillboardHelper.forwardDepth(cam, worldPos);
		float guiPixelToWorld = BillboardHelper.guiPixelToWorld(cam, FIXED_DEPTH);
		float factor = FIXED_DEPTH / depth;
		ps.translate(
				(worldPos.x - cam.pos.x) * factor,
				(worldPos.y - cam.pos.y) * factor,
				(worldPos.z - cam.pos.z) * factor);
		return guiPixelToWorld;
	}
}
