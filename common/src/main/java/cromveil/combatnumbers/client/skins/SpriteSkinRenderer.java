package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Matrix4fc;

public class SpriteSkinRenderer implements SkinRenderer {

	private final SpriteSheet spriteSheet;
	private final String text;
	private final int fillColor;
	private final float letterSpacing;
	private final float totalWidth;
	private final float charHeight;
	private final CharInfo[] charInfos;

	private record CharInfo(float x, float minU, float maxU, float minV, float maxV, float quadWidth) {
	}

	public SpriteSkinRenderer(SpriteSheet spriteSheet, String text, int fillColor, float letterSpacing) {
		this.spriteSheet = spriteSheet;
		this.text = text;
		this.fillColor = fillColor;
		this.letterSpacing = letterSpacing;
		this.charHeight = spriteSheet.cellHeight();
		this.charInfos = buildCharInfos();
		this.totalWidth = computeTotalWidth();
	}

	private CharInfo[] buildCharInfos() {
		CharInfo[] infos = new CharInfo[text.length()];
		float cursorX = 0f;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int index = spriteSheet.indexForChar(c);
			float advance = spriteSheet.advance(index);
			if (advance == 0.0f) {
				cursorX += letterSpacing;
				infos[i] = null;
				continue;
			}
			float offset = spriteSheet.uOffset(index);
			float cellUvWidth = spriteSheet.maxU(index) - spriteSheet.minU(index);
			float u0 = spriteSheet.minU(index) + offset * cellUvWidth;
			float u1 = u0 + advance * cellUvWidth;
			float v0 = spriteSheet.minV(index);
			float v1 = spriteSheet.maxV(index);
			float quadWidth = advance * spriteSheet.cellWidth();
			infos[i] = new CharInfo(cursorX, u0, u1, v0, v1, quadWidth);
			cursorX += quadWidth + letterSpacing;
		}
		return infos;
	}

	private float computeTotalWidth() {
		float w = 0f;
		for (int i = 0; i < text.length(); i++) {
			int index = spriteSheet.indexForChar(text.charAt(i));
			float advance = spriteSheet.advance(index);
			w += advance > 0.0f ? advance * spriteSheet.cellWidth() : 0f;
			if (i > 0)
				w += letterSpacing;
		}
		return w;
	}

	@Override
	public void render3d(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float alpha, int light) {
		int a = (int) (alpha * 255f);
		int color = (a << 24) | (fillColor & 0x00FFFFFF);

		float startX = -totalWidth / 2f;

		var renderType = RenderTypes.textSeeThrough(spriteSheet.textureId());

		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
			Matrix4fc matrix = pose.pose();
			renderChars(vertexConsumer, matrix, startX, 0f, color, light);
		});
	}

	private void renderChars(VertexConsumer buffer, Matrix4fc pose, float startX, float y, int color, int light) {
		for (int i = 0; i < text.length(); i++) {
			CharInfo info = charInfos[i];
			if (info == null)
				continue;
			float x0 = startX + info.x;
			float x1 = x0 + info.quadWidth;
			float y0 = y;
			float y1 = y + charHeight;
			buffer.addVertex(pose, x0, y0, 0f).setColor(color).setUv(info.minU, info.minV).setLight(light);
			buffer.addVertex(pose, x0, y1, 0f).setColor(color).setUv(info.minU, info.maxV).setLight(light);
			buffer.addVertex(pose, x1, y1, 0f).setColor(color).setUv(info.maxU, info.maxV).setLight(light);
			buffer.addVertex(pose, x1, y0, 0f).setColor(color).setUv(info.maxU, info.minV).setLight(light);
		}
	}

	private void renderChar(VertexConsumer buffer, Matrix4fc pose, int index, float startX, float y, int color,
			int light) {
		if (index < 0 || index >= charInfos.length)
			return;
		CharInfo info = charInfos[index];
		if (info == null)
			return;
		float x0 = startX + info.x;
		float x1 = x0 + info.quadWidth;
		float y0 = y;
		float y1 = y + charHeight;
		buffer.addVertex(pose, x0, y0, 0f).setColor(color).setUv(info.minU, info.minV).setLight(light);
		buffer.addVertex(pose, x0, y1, 0f).setColor(color).setUv(info.minU, info.maxV).setLight(light);
		buffer.addVertex(pose, x1, y1, 0f).setColor(color).setUv(info.maxU, info.maxV).setLight(light);
		buffer.addVertex(pose, x1, y0, 0f).setColor(color).setUv(info.maxU, info.minV).setLight(light);
	}

	@Override
	public void renderChar3d(int index, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			float alpha, int light) {
		int a = (int) (alpha * 255f);
		int color = (a << 24) | (fillColor & 0x00FFFFFF);

		float startX = -totalWidth / 2f;

		var renderType = RenderTypes.textSeeThrough(spriteSheet.textureId());

		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
			Matrix4fc matrix = pose.pose();
			renderChar(vertexConsumer, matrix, index, startX, 0f, color, light);
		});
	}

	@Override
	public void render2d(GuiGraphicsExtractor graphics, float alpha) {
		int a = (int) (alpha * 255f);
		if (a <= 0)
			return;
		int color = (a << 24) | (fillColor & 0x00FFFFFF);
		blitChars(graphics, -totalWidth / 2f, color, 0, text.length());
	}

	@Override
	public void renderChar2d(int index, GuiGraphicsExtractor graphics, float alpha) {
		int a = (int) (alpha * 255f);
		if (a <= 0 || index < 0 || index >= charInfos.length)
			return;
		int color = (a << 24) | (fillColor & 0x00FFFFFF);
		blitChars(graphics, -totalWidth / 2f, color, index, index + 1);
	}

	private void blitChars(GuiGraphicsExtractor graphics, float startX, int color, int from, int to) {
		int texWidth = spriteSheet.columns() * spriteSheet.cellWidth();
		int texHeight = spriteSheet.rows() * spriteSheet.cellHeight();
		for (int i = from; i < to; i++) {
			CharInfo info = charInfos[i];
			if (info == null)
				continue;
			int x = Math.round(startX + info.x);
			int width = Math.round(info.quadWidth);
			int height = Math.round(charHeight);
			int y = -(height / 2);
			float u = info.minU * texWidth;
			float v = info.minV * texHeight;
			graphics.blit(RenderPipelines.GUI_TEXTURED, spriteSheet.textureId(),
					x, y, u, v, width, height, texWidth, texHeight, color);
		}
	}
}
