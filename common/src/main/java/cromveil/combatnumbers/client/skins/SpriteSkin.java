package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.Constants;

import org.jspecify.annotations.Nullable;

public record SpriteSkin(SpriteSheet sheet, @Nullable Integer fillColor, float letterSpacing, boolean colored,
		float scale) implements Skin {
	public SpriteSkin(SpriteSheet sheet, @Nullable Integer fillColor, float letterSpacing, boolean colored) {
		this(sheet, fillColor, letterSpacing, colored, 1.0f);
	}

	@Override
	public SkinRenderer createVisual(String text) {
		if (sheet == null) {
			Constants.LOG.warn("SpriteSkin with null sheet, falling back to text");
			return new TextSkinRenderer(
					text,
					fillColor != null ? fillColor : 0xFFFFFFFF,
					0xFF000000);
		}
		int color;
		if (colored) {
			color = fillColor != null ? fillColor : 0xFFFFFFFF;
		} else {
			color = 0xFFFFFFFF;
		}
		return new SpriteSkinRenderer(sheet, text, color, letterSpacing);
	}

	@Override
	public float getScale() {
		return scale;
	}
}
