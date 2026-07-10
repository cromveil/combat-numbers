package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
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
		// graphics.blit(...) doesn't support colors/alpha, doing it manually
		float a = ((color >> 24) & 0xFF) / 255f;
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

		RenderSystem.setShaderTexture(0, StableIdMapper.to(texture));
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_TEX_COLOR);
		Matrix4f matrix = graphics.pose().last().pose();

		float u0 = u / textureWidth;
		float v0 = v / textureHeight;
		float u1 = (u + width) / textureWidth;
		float v1 = (v + height) / textureHeight;

		buffer.addVertex(matrix, x, y + height, 0).setColor(r, g, b, a).setUv(u0, v1);
		buffer.addVertex(matrix, x + width, y + height, 0).setColor(r, g, b, a).setUv(u1, v1);
		buffer.addVertex(matrix, x + width, y, 0).setColor(r, g, b, a).setUv(u1, v0);
		buffer.addVertex(matrix, x, y, 0).setColor(r, g, b, a).setUv(u0, v0);

		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.disableBlend();
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
