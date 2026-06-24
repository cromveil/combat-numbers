package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.config.ModConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class FloatingTextRenderer {

	private FloatingTextRenderer() {
	}

	private static final float BASE_SCALE_FACTOR = 0.015f;
	private static final float MIN_RENDER_DISTANCE = 0.5f;

	public static void renderAll(
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			CameraRenderState cameraState) {
		Vec3 camPos = cameraState.pos;
		Frustum frustum = cameraState.cullFrustum;
		var config = ModConfig.getInstance();

		for (FloatingText text : FloatingTextManager.getActive()) {
			Vec3 renderPos = text.getRenderPos();

			if (!frustum.pointInFrustum(renderPos.x, renderPos.y, renderPos.z))
				continue;

			float alpha = text.getAlpha();
			if (alpha <= 0f)
				continue;

			double distance = camPos.distanceTo(renderPos);
			float nearFade = nearFadeFactor(distance, config.nearFadeDistance);
			float distanceAlpha = alpha * nearFade;
			if (distanceAlpha <= 0f)
				continue;

			float baseScale = BASE_SCALE_FACTOR * config.baseSize * text.scale;
			float distanceScale = computeScale(distance, baseScale,
					config.distanceFalloffStart, config.distanceFalloffEnd, config.distanceMinScale);

			int light = LightCoordsUtil.FULL_BRIGHT;

			if (text.hasPerCharStagger()) {
				renderStaggered(text, camPos, cameraState, config, distanceScale, light,
						poseStack, submitNodeCollector);
			} else {
				renderWhole(text, camPos, cameraState, distanceAlpha, distanceScale, light,
						poseStack, submitNodeCollector);
			}
		}
	}

	private static void renderWhole(FloatingText text, Vec3 camPos, CameraRenderState cameraState,
			float alpha, float distanceScale, int light,
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		float renderScale = distanceScale * text.getScaleMultiplier();
		float rotDeg = text.getRotation();

		poseStack.pushPose();
		Vec3 rp = text.getRenderPos();
		poseStack.translate(rp.x - camPos.x, rp.y - camPos.y, rp.z - camPos.z);
		poseStack.mulPose(cameraState.orientation);

		if (rotDeg != 0f) {
			poseStack.rotateAround(
					new Quaternionf().rotationZ((float) Math.toRadians(rotDeg)),
					0f, 0f, 0f);
		}

		poseStack.scale(renderScale, -renderScale, renderScale);
		text.visual.render(poseStack, submitNodeCollector, alpha, light);
		poseStack.popPose();
	}

	private static void renderStaggered(FloatingText text, Vec3 camPos, CameraRenderState cameraState,
			ModConfig config, float distanceScale, int light,
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		int charCount = text.getCharCount();
		for (int c = 0; c < charCount; c++) {
			Vec3 charRenderPos = text.getCharRenderPos(c);
			float charAlpha = text.getCharAlpha(c);

			double charDist = camPos.distanceTo(charRenderPos);
			float nearFade = nearFadeFactor(charDist, config.nearFadeDistance);
			float charDistAlpha = charAlpha * nearFade;
			if (charDistAlpha <= 0f)
				continue;

			float charScale = distanceScale * text.getCharScaleMultiplier(c);
			float charRotDeg = text.getCharRotation(c);

			poseStack.pushPose();
			poseStack.translate(
					charRenderPos.x - camPos.x,
					charRenderPos.y - camPos.y,
					charRenderPos.z - camPos.z);
			poseStack.mulPose(cameraState.orientation);

			if (charRotDeg != 0f) {
				poseStack.rotateAround(
						new Quaternionf().rotationZ((float) Math.toRadians(charRotDeg)),
						0f, 0f, 0f);
			}

			poseStack.scale(charScale, -charScale, charScale);
			text.visual.renderChar(c, poseStack, submitNodeCollector, charDistAlpha, light);
			poseStack.popPose();
		}
	}

	private static float nearFadeFactor(double distance, float nearFadeDistance) {
		return (float) Math.clamp(
				(distance - MIN_RENDER_DISTANCE) / (nearFadeDistance - MIN_RENDER_DISTANCE),
				0.0, 1.0);
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
