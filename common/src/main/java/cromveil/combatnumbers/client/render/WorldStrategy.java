package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

/**
 * Renders on true world position, was my first implementation and probably
 * wasn't the best decision. Suffers from translucent objects like water being
 * drawn over it.
 */
public final class WorldStrategy extends BillboardStrategy {

	WorldStrategy(PoseStack ps, GeometrySubmitter geom, RenderCamera cam) {
		super(ps, geom, cam);
	}

	@Override
	protected float anchor(Vec3 worldPos) {
		float depth = BillboardHelper.forwardDepth(cam, worldPos);
		float guiPixelToWorld = BillboardHelper.guiPixelToWorld(cam, depth);
		Vec3 camPos = cam.position();
		ps.translate(
				worldPos.x - camPos.x,
				worldPos.y - camPos.y,
				worldPos.z - camPos.z);
		return guiPixelToWorld;
	}
}
