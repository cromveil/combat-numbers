package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class TextSkinRenderer implements SkinRenderer {
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
	public void render3d(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float alpha, int light) {
		Font font = Minecraft.getInstance().font;
		float x = -font.width(text) / 2f;
		submitText(font, fullSequence, x, 0f, alpha, poseStack, submitNodeCollector, light);
	}

	@Override
	public void renderChar3d(int index, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			float alpha, int light) {
		Font font = Minecraft.getInstance().font;
		submitText(font, charSequences[index], charStartX(font, index), 0f, alpha,
				poseStack, submitNodeCollector, light);
	}

	private void submitText(Font font, FormattedCharSequence sequence, float x, float y, float alpha,
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
		int a = (int) (alpha * 255f);
		if (a <= 0)
			return;
		int fColor = (a << 24) | (fillColor & 0x00FFFFFF);
		int oColor = (a << 24) | (outlineColor & 0x00FFFFFF);

		GlyphCollector fill = collectGlyphs(font, sequence, x, y, fColor);
		if (fill.isEmpty())
			return;

		List<GlyphCollector> outlines = new ArrayList<>(OUTLINE_OFFSETS.length);
		for (int[] off : OUTLINE_OFFSETS) {
			outlines.add(collectGlyphs(font, sequence, x + off[0], y + off[1], oColor));
		}

		submitNodeCollector.submitCustomGeometry(poseStack, fill.renderType, (pose, vertexConsumer) -> {
			for (GlyphCollector outline : outlines) {
				for (TextRenderable glyph : outline.glyphs) {
					glyph.render(pose.pose(), vertexConsumer, light, false);
				}
			}
			for (TextRenderable glyph : fill.glyphs) {
				glyph.render(pose.pose(), vertexConsumer, light, false);
			}
		});
	}

	@Override
	public void render2d(GuiGraphicsExtractor graphics, float alpha) {
		int a = (int) (alpha * 255f);
		if (a <= 0)
			return;
		Font font = Minecraft.getInstance().font;
		int x = -(font.width(text) / 2);
		int y = -(font.lineHeight / 2);
		drawText(graphics, font, fullSequence, x, y, a);
	}

	@Override
	public void renderChar2d(int index, GuiGraphicsExtractor graphics, float alpha) {
		int a = (int) (alpha * 255f);
		if (a <= 0)
			return;
		Font font = Minecraft.getInstance().font;
		int x = -(font.width(text) / 2) + font.width(text.substring(0, index));
		int y = -(font.lineHeight / 2);
		drawText(graphics, font, charSequences[index], x, y, a);
	}

	private void drawText(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence sequence,
			int x, int y, int a) {
		int fColor = (a << 24) | (fillColor & 0x00FFFFFF);
		int oColor = (a << 24) | (outlineColor & 0x00FFFFFF);
		for (int[] off : OUTLINE_OFFSETS) {
			graphics.text(font, sequence, x + off[0], y + off[1], oColor, false);
		}
		graphics.text(font, sequence, x, y, fColor, false);
	}

	private float charStartX(Font font, int index) {
		return -font.width(text) / 2f + font.width(text.substring(0, index));
	}

	private static GlyphCollector collectGlyphs(Font font, FormattedCharSequence sequence,
			float x, float y, int color) {
		var pt = font.prepareText(sequence, x, y, color, false, true, 0);
		var renderables = new ArrayList<TextRenderable>();
		pt.visit(new Font.GlyphVisitor() {
			@Override
			public void acceptGlyph(TextRenderable.Styled glyph) {
				renderables.add(glyph);
			}
		});

		if (renderables.isEmpty())
			return new GlyphCollector(List.of(), null);

		RenderType renderType = renderables.getFirst().renderType(Font.DisplayMode.SEE_THROUGH);
		return new GlyphCollector(renderables, renderType);
	}

	private record GlyphCollector(List<TextRenderable> glyphs, RenderType renderType) {
		boolean isEmpty() {
			return glyphs.isEmpty();
		}
	}
}
