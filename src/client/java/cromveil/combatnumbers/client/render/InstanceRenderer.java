package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.config.ModConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class InstanceRenderer {

	private InstanceRenderer() {}

	private static final float BASE_SCALE_FACTOR = 0.020f;
	private static final float MIN_RENDER_DISTANCE = 0.5f;

	public static void renderAll(
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			CameraRenderState cameraState,
			double currentGameTime
	) {
		Vec3 camPos = cameraState.pos;
		Frustum frustum = cameraState.cullFrustum;
		var config = ModConfig.getInstance();

		for (Instance instance : InstanceManager.getActiveInstances()) {
			Vec3 renderPos = instance.getRenderPos(currentGameTime);

			if (!frustum.pointInFrustum(renderPos.x, renderPos.y, renderPos.z)) continue;

			double distance = camPos.distanceTo(renderPos);
			float alpha = instance.getAlpha(currentGameTime);
			if (alpha <= 0f) continue;

			float nearFade = (float) Math.clamp(
				(distance - MIN_RENDER_DISTANCE) / (config.nearFadeDistance - MIN_RENDER_DISTANCE),
				0.0, 1.0);
			alpha *= nearFade;
			if (alpha <= 0f) continue;

			float scale = computeScale(distance,
				BASE_SCALE_FACTOR * config.baseSize * instance.scale * instance.getScaleMultiplier(currentGameTime),
				config.distanceFalloffStart, config.distanceFalloffEnd, config.distanceMinScale);

			poseStack.pushPose();

			poseStack.translate(
				renderPos.x - camPos.x,
				renderPos.y - camPos.y,
				renderPos.z - camPos.z
			);

			poseStack.mulPose(cameraState.orientation);

			float rotDeg = instance.getRotation(currentGameTime);
			if (rotDeg != 0f) {
				poseStack.rotateAround(
					new Quaternionf().rotationZ((float) Math.toRadians(rotDeg)),
					0f, 0f, 0f
				);
			}

			poseStack.scale(scale, -scale, scale);

			int light = LightCoordsUtil.FULL_BRIGHT;
			instance.visual.render(
				poseStack, submitNodeCollector, alpha, light
			);

			poseStack.popPose();
		}
	}

	private static float computeScale(double distance, float baseScale,
			float falloffStart, float falloffEnd, float minScale) {
		float d = (float) Math.max(distance, MIN_RENDER_DISTANCE);
		float perceivedScale;
		if (d <= falloffStart) {
			perceivedScale = 1.0f;
		} else if (d >= falloffEnd) {
			perceivedScale = minScale;
		} else {
			float t = (d - falloffStart) / (falloffEnd - falloffStart);
			perceivedScale = 1.0f + (minScale - 1.0f) * t;
		}
		float correction = perceivedScale * d / falloffStart;
		return baseScale * correction;
	}
}
