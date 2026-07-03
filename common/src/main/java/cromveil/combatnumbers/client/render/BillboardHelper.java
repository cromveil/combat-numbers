package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class BillboardHelper {

	private static final float NDC_MARGIN = 1.1f;

	private BillboardHelper() {
	}

	public static float fontReferenceHeight() {
		return Minecraft.getInstance().font.lineHeight;
	}

	public static float forwardDepth(RenderCamera cam, Vec3 worldPos) {
		Vec3 camPos = cam.position();
		Vector4f v = new Vector4f(
				(float) (worldPos.x - camPos.x),
				(float) (worldPos.y - camPos.y),
				(float) (worldPos.z - camPos.z),
				1.0f);
		cam.viewRotation().transform(v);
		float depth = -v.z;
		return depth < 0.05f ? 0.05f : depth;
	}

	public static float guiPixelToWorld(RenderCamera cam, float depth) {
		int guiScaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		float focalLength = Math.abs(cam.projection().m11());
		if (focalLength < 1.0e-6f || guiScaledHeight <= 0) {
			return 0f;
		}
		return depth * 2.0f / (focalLength * guiScaledHeight);
	}

	public static void faceCamera(PoseStack ps, RenderCamera cam) {
		var invView = new Matrix4f(cam.viewRotation()).invertAffine();
		ps.mulPose(invView);
	}

	public static void rotateZ(PoseStack ps, float degrees) {
		if (degrees != 0f) {
			ps.rotateAround(
					new Quaternionf().rotationZ((float) Math.toRadians(degrees)),
					0f, 0f, 0f);
		}
	}

	public static void translatePixelOffsets(PoseStack ps, float px, float py, float guiPixelToWorld,
			float perspectiveScale) {
		float d = guiPixelToWorld * perspectiveScale;
		ps.translate(px * d, py * d, 0f);
	}

	public static void scaleBillboard(PoseStack ps, float pixelSize, float guiPixelToWorld,
			float perspectiveScale) {
		float guiPixelHeight = pixelSize * perspectiveScale / fontReferenceHeight();
		float ws = guiPixelHeight * guiPixelToWorld;
		ps.scale(ws, -ws, ws);
	}

	/**
	 * NDC-based point culling — replaces the version-dependent
	 * {@code Frustum.pointInFrustum()}. Works identically on every
	 * Minecraft version because it only uses the view-rotation and
	 * projection matrices stored in {@link RenderCamera}.
	 *
	 * @return {@code true} if the world-space point should be
	 *         rendered (i.e. it projects inside the screen).
	 */
	public static boolean isOnScreen(RenderCamera cam, Vec3 worldPos) {
		Vec3 camPos = cam.position();
		Vector4f clip = new Vector4f(
				(float) (worldPos.x - camPos.x),
				(float) (worldPos.y - camPos.y),
				(float) (worldPos.z - camPos.z),
				1.0f);
		cam.viewRotation().transform(clip);
		cam.projection().transform(clip);
		if (Math.abs(clip.w) <= 1.0e-4f) {
			return false;
		}
		float ndcX = clip.x / clip.w;
		float ndcY = clip.y / clip.w;
		return !(ndcX < -NDC_MARGIN || ndcX > NDC_MARGIN
				|| ndcY < -NDC_MARGIN || ndcY > NDC_MARGIN);
	}
}
