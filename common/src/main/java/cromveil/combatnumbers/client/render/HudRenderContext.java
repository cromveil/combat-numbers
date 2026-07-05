package cromveil.combatnumbers.client.render;

import cromveil.combatnumbers.core.StableId;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;

public interface HudRenderContext {

	void pushMatrix();

	void popMatrix();

	void translate(float x, float y);

	void scale(float x, float y);

	void rotate(float radians);

	void drawString(Font font, FormattedCharSequence sequence, int x, int y, int color,
			boolean shadow);

	void blitSprite(StableId texture, int x, int y, float u, float v, int width,
			int height, int textureWidth, int textureHeight, int color);

	int guiWidth();

	int guiHeight();
}
