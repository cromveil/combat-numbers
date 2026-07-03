package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

/**
 * Fixed view-space depth, was actually easier to implement than world strategy,
 * but still suffers from translucent objects like water being drawn over it.
 */
public final class ScreenStrategy extends BillboardStrategy {

	private static final float FIXED_DEPTH = 1.0f;

	ScreenStrategy(PoseStack ps, GeometrySubmitter geom, RenderCamera cam) {
		super(ps, geom, cam);
	}

	@Override
	protected float anchor(Vec3 worldPos) {
		float depth = BillboardHelper.forwardDepth(cam, worldPos);
		float guiPixelToWorld = BillboardHelper.guiPixelToWorld(cam, FIXED_DEPTH);
		float factor = FIXED_DEPTH / depth;
		Vec3 camPos = cam.position();
		ps.translate(
				(worldPos.x - camPos.x) * factor,
				(worldPos.y - camPos.y) * factor,
				(worldPos.z - camPos.z) * factor);
		return guiPixelToWorld;
	}
}
