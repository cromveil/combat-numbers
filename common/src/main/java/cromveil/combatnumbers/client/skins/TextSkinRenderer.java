package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import cromveil.combatnumbers.client.render.IGeometrySubmitter;
import cromveil.combatnumbers.client.render.IHudRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class TextSkinRenderer implements ISkinRenderer {
	private static final int[][] OUTLINE_OFFSETS = {
			{ -1, -1 }, { 0, -1 }, { 1, -1 },
			{ -1, 0 }, { 1, 0 },
			{ -1, 1 }, { 0, 1 }, { 1, 1 }
	};

	private final String text;
	private final int fillColor;
	private final int outlineColor;
	private final FormattedCharSequence fullSequence;
	private final FormattedCharSequence[] charSequences;

	public TextSkinRenderer(String text, int fillColor, int outlineColor) {
		this.text = text;
		this.fillColor = fillColor;
		this.outlineColor = outlineColor;
		this.fullSequence = FormattedCharSequence.forward(text, Style.EMPTY);
		this.charSequences = new FormattedCharSequence[text.length()];
		for (int i = 0; i < text.length(); i++) {
			this.charSequences[i] = FormattedCharSequence.forward(
					String.valueOf(text.charAt(i)), Style.EMPTY);
		}
	}

	@Override
	public void render3d(PoseStack poseStack, IGeometrySubmitter geom, float alpha, int light) {
		if (alpha <= 0f)
			return;
		int a = (int) (alpha * 255f);
		int fColor = (a << 24) | (fillColor & 0x00FFFFFF);
		int oColor = (a << 24) | (outlineColor & 0x00FFFFFF);

		Font font = Minecraft.getInstance().font;
		float x = -font.width(text) / 2f;

		if (geom instanceof MultiBufferSource mbs) {
			font.drawInBatch8xOutline(fullSequence, x, 0f, fColor, oColor,
					poseStack.last().pose(), mbs, light);
		}
	}

	@Override
	public void renderChar3d(int index, PoseStack poseStack, IGeometrySubmitter geom,
			float alpha, int light) {
		if (alpha <= 0f || index < 0 || index >= text.length())
			return;
		int a = (int) (alpha * 255f);
		int fColor = (a << 24) | (fillColor & 0x00FFFFFF);
		int oColor = (a << 24) | (outlineColor & 0x00FFFFFF);

		Font font = Minecraft.getInstance().font;
		float x = charStartX(font, index);

		if (geom instanceof MultiBufferSource mbs) {
			font.drawInBatch8xOutline(charSequences[index], x, 0f, fColor, oColor,
					poseStack.last().pose(), mbs, light);
		}
	}

	@Override
	public void render2d(IHudRenderContext ctx, float alpha) {
		int a = (int) (alpha * 255f);
		if (a <= 0)
			return;
		Font font = Minecraft.getInstance().font;
		int x = -(font.width(text) / 2);
		int y = -(font.lineHeight / 2);
		drawText(ctx, font, fullSequence, x, y, a);
	}

	@Override
	public void renderChar2d(int index, IHudRenderContext ctx, float alpha) {
		int a = (int) (alpha * 255f);
		if (a <= 0)
			return;
		Font font = Minecraft.getInstance().font;
		int x = -(font.width(text) / 2) + font.width(text.substring(0, index));
		int y = -(font.lineHeight / 2);
		drawText(ctx, font, charSequences[index], x, y, a);
	}

	private void drawText(IHudRenderContext ctx, Font font, FormattedCharSequence sequence,
			int x, int y, int a) {
		int fColor = (a << 24) | (fillColor & 0x00FFFFFF);
		int oColor = (a << 24) | (outlineColor & 0x00FFFFFF);
		for (int[] off : OUTLINE_OFFSETS) {
			ctx.drawString(font, sequence, x + off[0], y + off[1], oColor, false);
		}
		ctx.drawString(font, sequence, x, y, fColor, false);
	}

	private float charStartX(Font font, int index) {
		return -font.width(text) / 2f + font.width(text.substring(0, index));
	}
}
