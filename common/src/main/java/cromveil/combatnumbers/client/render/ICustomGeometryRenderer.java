package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Callback that receives a pre-transformed pose and a vertex consumer
 * ready for custom geometry emission.
 * <p>
 * Mirrors the Minecraft {@code SubmitNodeCollector.ICustomGeometryRenderer}
 * contract but lives in our namespace so that skin renderers never
 * has to import a version-varying class.
 */
@FunctionalInterface
public interface ICustomGeometryRenderer {
	void render(PoseStack.Pose pose, VertexConsumer vertexConsumer);
}
