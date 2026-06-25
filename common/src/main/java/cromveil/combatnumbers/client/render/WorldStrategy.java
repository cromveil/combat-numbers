package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Renders on true world position, was my first implementation and probably
 * wasn't the best decision. Suffers from translucent objects like water being
 * drawn over it.
 */
public final class WorldStrategy extends BillboardStrategy {

	WorldStrategy(PoseStack ps, SubmitNodeCollector collector, CameraRenderState cam) {
		super(ps, collector, cam);
	}

	@Override
	protected float anchor(Vec3 worldPos) {
		float depth = BillboardMath.forwardDepth(cam, worldPos);
		float guiPixelToWorld = BillboardMath.guiPixelToWorld(cam, depth);
		ps.translate(
				worldPos.x - cam.pos.x,
				worldPos.y - cam.pos.y,
				worldPos.z - cam.pos.z);
		return guiPixelToWorld;
	}
}
