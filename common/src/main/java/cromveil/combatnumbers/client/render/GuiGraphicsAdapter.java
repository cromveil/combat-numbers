package cromveil.combatnumbers.client.render;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Quaternionf;

public final class GuiGraphicsAdapter implements IHudRenderContext {

	private final GuiGraphics graphics;

	public GuiGraphicsAdapter(GuiGraphics graphics) {
		this.graphics = graphics;
	}

	@Override
	public void pushMatrix() {
		graphics.pose().pushPose();
	}

	@Override
	public void popMatrix() {
		graphics.pose().popPose();
	}

	@Override
	public void translate(float x, float y) {
		graphics.pose().translate(x, y, 0f);
	}

	@Override
	public void scale(float x, float y) {
		graphics.pose().scale(x, y, 1f);
	}

	@Override
	public void rotate(float radians) {
		graphics.pose().mulPose(new Quaternionf().rotationZ(radians));
	}

	@Override
	public void drawString(Font font, FormattedCharSequence sequence, int x, int y, int color,
			boolean shadow) {
		graphics.drawString(font, sequence, x, y, color, shadow);
	}

	@Override
	public void blitSprite(StableId texture, int x, int y, float u, float v, int width,
			int height, int textureWidth, int textureHeight, int color) {
		graphics.blit(StableIdMapper.to(texture), x, y, u, v,
				width, height, textureWidth, textureHeight);
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
