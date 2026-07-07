package cromveil.combatnumbers.client.render;

import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Creates a {@link RenderCamera} from version-specific camera types.
 * <p>
 * On 1.21.11 the camera state carries only position + orientation.
 * The view-rotation and projection matrices are captured once per
 * frame by {@code LevelRenderStateCaptureMixin} into {@link RenderStateCache}.
 */
public final class CameraAdapter {

	private CameraAdapter() {
	}

	public static RenderCamera from(CameraRenderState cam) {
		return new RenderCamera(cam.pos,
				new Matrix4f(RenderStateCache.viewRotationMatrix()),
				new Matrix4f(RenderStateCache.projectionMatrix()),
				new Quaternionf(cam.orientation));
	}
}
