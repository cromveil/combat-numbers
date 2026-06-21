package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class TextSkinRenderer implements SkinRenderer {
	private final String text;
	private final int fillColor;
	private final int outlineColor;

	public TextSkinRenderer(String text, int fillColor, int outlineColor) {
		this.text = text;
		this.fillColor = fillColor;
		this.outlineColor = outlineColor;
	}

	@Override
	public void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float alpha, int light) {
		Font font = Minecraft.getInstance().font;
		float textWidth = font.width(text);
		float x = -textWidth / 2f;
		float y = 0f;
		int a = (int) (alpha * 255f);
		int fColor = (a << 24) | (fillColor & 0x00FFFFFF);
		int oColor = (a << 24) | (outlineColor & 0x00FFFFFF);

		FormattedCharSequence formatted = FormattedCharSequence.forward(text, Style.EMPTY);

		final float outlineOffset = 1.0f;
		for (int ox = -1; ox <= 1; ox++) {
			for (int oy = -1; oy <= 1; oy++) {
				if (ox == 0 && oy == 0) continue;
				submitNodeCollector.submitText(
					poseStack,
					x + ox * outlineOffset,
					y + oy * outlineOffset,
					formatted,
					false,
					Font.DisplayMode.SEE_THROUGH,
					light,
					oColor,
					0,
					0
				);
			}
		}

		submitNodeCollector.submitText(
			poseStack,
			x,
			y,
			formatted,
			false,
			Font.DisplayMode.SEE_THROUGH,
			light,
			fColor,
			0,
			0
		);
	}
}
