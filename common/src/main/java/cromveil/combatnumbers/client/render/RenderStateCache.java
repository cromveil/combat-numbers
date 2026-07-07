package cromveil.combatnumbers.client.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class RenderStateCache {

	private static final Matrix4f projectionMatrix = new Matrix4f();
	private static final Matrix4f viewRotationMatrix = new Matrix4f();
	private static Frustum cullFrustum = new Frustum(new Matrix4f(), new Matrix4f());

	private RenderStateCache() {
	}

	public static void capture(Matrix4f projForCulling, Matrix4f viewRotation, Vec3 camPos) {
		projectionMatrix.set(projForCulling);
		viewRotationMatrix.set(viewRotation);
		cullFrustum = new Frustum(viewRotation, projForCulling);
		cullFrustum.prepare(camPos.x, camPos.y, camPos.z);
	}

	public static Matrix4f projectionMatrix() {
		return projectionMatrix;
	}

	public static Matrix4f viewRotationMatrix() {
		return viewRotationMatrix;
	}

	public static Frustum cullFrustum() {
		return cullFrustum;
	}
}
