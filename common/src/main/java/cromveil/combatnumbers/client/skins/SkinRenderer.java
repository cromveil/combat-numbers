package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;

public interface SkinRenderer {
	void render3d(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float alpha, int light);

	default void renderChar3d(int index, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			float alpha, int light) {
		render3d(poseStack, submitNodeCollector, alpha, light);
	}

	default void render2d(GuiGraphicsExtractor graphics, float alpha) {
	}

	default void renderChar2d(int index, GuiGraphicsExtractor graphics, float alpha) {
		render2d(graphics, alpha);
	}
}
