package cromveil.combatnumbers.client.render;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.FormattedCharSequence;

public final class GuiGraphicsAdapter implements IHudRenderContext {

	private final GuiGraphics graphics;

	public GuiGraphicsAdapter(GuiGraphics graphics) {
		this.graphics = graphics;
	}

	@Override
	public void pushMatrix() {
		graphics.pose().pushMatrix();
	}

	@Override
	public void popMatrix() {
		graphics.pose().popMatrix();
	}

	@Override
	public void translate(float x, float y) {
		graphics.pose().translate(x, y);
	}

	@Override
	public void scale(float x, float y) {
		graphics.pose().scale(x, y);
	}

	@Override
	public void rotate(float radians) {
		graphics.pose().rotate(radians);
	}

	@Override
	public void drawString(Font font, FormattedCharSequence sequence, int x, int y, int color,
			boolean shadow) {
		graphics.drawString(font, sequence, x, y, color, shadow);
	}

	@Override
	public void blitSprite(StableId texture, int x, int y, float u, float v, int width,
			int height, int textureWidth, int textureHeight, int color) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, StableIdMapper.to(texture), x, y, u, v,
				width, height, textureWidth, textureHeight, color);
	}

	@Override
	public int guiWidth() {
		return graphics.guiWidth();
	}

	@Override
	public int guiHeight() {
		return graphics.guiHeight();
	}
}
