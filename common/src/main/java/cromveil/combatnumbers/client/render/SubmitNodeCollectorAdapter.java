package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Bridges 26.2 / 1.21.11's {@code SubmitNodeCollector} to the
 * version-agnostic {@link GeometrySubmitter}.
 */
public final class SubmitNodeCollectorAdapter implements GeometrySubmitter {

	private final SubmitNodeCollector collector;

	public SubmitNodeCollectorAdapter(SubmitNodeCollector collector) {
		this.collector = collector;
	}

	@Override
	public void submitGeometry(PoseStack poseStack, RenderType renderType,
			CustomGeometryRenderer renderer) {
		collector.submitCustomGeometry(poseStack, renderType,
				(pose, vertexConsumer) -> renderer.render(pose, vertexConsumer));
	}
}
