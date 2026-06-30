package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class BillboardHelper {

	private BillboardHelper() {
	}

	public static float fontReferenceHeight() {
		return Minecraft.getInstance().font.lineHeight;
	}

	public static float forwardDepth(CameraRenderState cam, Vec3 worldPos) {
		Vector4f v = new Vector4f(
				(float) (worldPos.x - cam.pos.x),
				(float) (worldPos.y - cam.pos.y),
				(float) (worldPos.z - cam.pos.z),
				1.0f);
		cam.viewRotationMatrix.transform(v);
		float depth = -v.z;
		return depth < 0.05f ? 0.05f : depth;
	}

	public static float guiPixelToWorld(CameraRenderState cam, float depth) {
		int guiScaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		float focalLength = Math.abs(cam.projectionMatrix.m11());
		if (focalLength < 1.0e-6f || guiScaledHeight <= 0) {
			return 0f;
		}
		return depth * 2.0f / (focalLength * guiScaledHeight);
	}

	public static void faceCamera(PoseStack ps, CameraRenderState cam) {
		var invView = new Matrix4f(cam.viewRotationMatrix).invertAffine();
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
}
