package cromveil.combatnumbers.client.render;

import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Creates a {@link RenderCamera} from version-specific camera types.
 * <p>
 * On 1.21.1 the camera is the mutable {@link Camera} object; the
 * view-rotation and projection matrices are captured once per frame
 * by {@code LevelRenderStateCaptureMixin} into {@link RenderStateCache}.
 */
public final class CameraAdapter {

	private CameraAdapter() {
	}

	public static RenderCamera from(Camera camera) {
		return new RenderCamera(camera.getPosition(),
				new Matrix4f(RenderStateCache.viewRotationMatrix()),
				new Matrix4f(RenderStateCache.projectionMatrix()),
				new Quaternionf(camera.rotation()));
	}
}
