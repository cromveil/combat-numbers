package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

public interface SkinRenderer {
	void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float alpha, int light);

	default void renderChar(int index, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			float alpha, int light) {
		render(poseStack, submitNodeCollector, alpha, light);
	}
}
