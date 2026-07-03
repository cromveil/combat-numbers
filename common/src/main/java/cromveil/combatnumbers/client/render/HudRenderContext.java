package cromveil.combatnumbers.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/**
 * Abstracts 2D HUD-rendering primitives so that skin renderers
 * and {@link HudStrategy} never import a version-varying graphics class
 * ({@code GuiGraphicsExtractor} / {@code GuiGraphics}).
 * <p>
 * For each Minecraft version a tiny adapter implements this interface
 * and forwards calls to the real graphics object.
 */
public interface HudRenderContext {

	void pushMatrix();

	void popMatrix();

	void translate(float x, float y);

	void scale(float x, float y);

	void rotate(float radians);

	void drawString(Font font, FormattedCharSequence sequence, int x, int y, int color,
			boolean shadow);

	void blitSprite(Identifier texture, int x, int y, float u, float v, int width,
			int height, int textureWidth, int textureHeight, int color);

	int guiWidth();

	int guiHeight();
}
