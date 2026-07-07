package cromveil.combatnumbers.client.render;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Creates a {@link RenderCamera} from version-specific camera types.
 * <p>
 * This is the <b>only</b> file that imports {@code CameraRenderState}
 * outside of mixin classes. When backporting, replace the implementation
 * here with the version-appropriate source (e.g. {@code RenderStateCache}
 * on 1.21.11, or manually captured matrices on 1.21.1).
 */
public final class CameraAdapter {

	private CameraAdapter() {
	}

	public static RenderCamera from(CameraRenderState cam) {
		return new RenderCamera(cam.pos,
				new Matrix4f(cam.viewRotationMatrix),
				new Matrix4f(cam.projectionMatrix),
				new Quaternionf(cam.orientation));
	}
}
