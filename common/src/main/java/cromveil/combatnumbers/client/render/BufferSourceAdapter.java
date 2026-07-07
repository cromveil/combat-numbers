package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Bridges 1.21.1's {@code MultiBufferSource.BufferSource} to the
 * version-agnostic {@link IGeometrySubmitter}.
 * <p>
 * Also implements {@link MultiBufferSource} so that font-based skin
 * renderers can call {@code Font.drawInBatch()} directly.
 */
public final class BufferSourceAdapter implements IGeometrySubmitter, MultiBufferSource {

	private final MultiBufferSource.BufferSource bufferSource;

	public BufferSourceAdapter(MultiBufferSource.BufferSource bufferSource) {
		this.bufferSource = bufferSource;
	}

	@Override
	public void submitGeometry(PoseStack poseStack, RenderType renderType,
			ICustomGeometryRenderer renderer) {
		var vc = bufferSource.getBuffer(renderType);
		renderer.render(poseStack.last(), vc);
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return bufferSource.getBuffer(renderType);
	}
}
