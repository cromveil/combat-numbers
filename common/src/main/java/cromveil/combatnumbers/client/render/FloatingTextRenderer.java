package cromveil.combatnumbers.client.render;

import cromveil.combatnumbers.config.ModConfig;
import net.minecraft.world.phys.Vec3;

public final class FloatingTextRenderer {

	private FloatingTextRenderer() {
	}

	private static final float MIN_FADE_DISTANCE = 0.5f;

	public static void renderAll(Strategy strategy) {
		var config = ModConfig.getInstance();
		float basePixelSize = config.basePixelSize;
		float nearFadeDistance = config.nearFadeDistance;
		float maxRenderDistance = config.maxRenderDistance;
		float falloffStart = config.distanceFalloffStart;
		float falloffEnd = config.distanceFalloffEnd;
		float minScale = config.distanceMinScale;
		float fontRef = BillboardMath.fontReferenceHeight();
		Vec3 camPos = strategy.camPos();

		for (FloatingText text : FloatingTextManager.getActive()) {
			Vec3 worldPos = text.worldPos;

			float alpha = text.getAlpha();
			if (alpha <= 0f)
				continue;

			double distance = camPos.distanceTo(worldPos);
			if (distance > maxRenderDistance)
				continue;

			if (distance < nearFadeDistance) {
				float t = (float) Math.clamp(
						(distance - MIN_FADE_DISTANCE) / (nearFadeDistance - MIN_FADE_DISTANCE),
						0.0, 1.0);
				alpha *= t;
				if (alpha <= 0f)
					continue;
			}

			if (strategy.cull(text))
				continue;

			float perceivedScale;
			if (distance <= falloffStart) {
				perceivedScale = 1.0f;
			} else if (distance >= falloffEnd) {
				perceivedScale = minScale;
			} else {
				float t = (float) ((distance - falloffStart) / (falloffEnd - falloffStart));
				perceivedScale = 1.0f + (minScale - 1.0f) * t;
			}

			float animScale = text.getScaleMultiplier();
			float scale = basePixelSize * text.scale * animScale;
			float offsetScale = basePixelSize / fontRef;
			float rot = text.getRotation();
			float offX = text.getOffsetX() * offsetScale;
			float offY = text.getOffsetY() * offsetScale;

			if (text.hasPerCharStagger()) {
				int charCount = text.getCharCount();
				for (int c = 0; c < charCount; c++) {
					float charAlpha = text.getCharAlpha(c) * alpha;
					if (charAlpha <= 0f)
						continue;
					float charScale = scale * text.getCharScaleMultiplier(c);
					float charRot = text.getCharRotation(c);
					float charOffX = text.getCharOffsetX(c) * offsetScale;
					float charOffY = text.getCharOffsetY(c) * offsetScale;
					strategy.draw(text, c, true, new Strategy.GlyphPlacement(
							charOffX, charOffY, charScale, charRot, perceivedScale, charAlpha));
				}
			} else {
				strategy.draw(text, 0, false, new Strategy.GlyphPlacement(
						offX, offY, scale, rot, perceivedScale, alpha));
			}
		}
	}
}
