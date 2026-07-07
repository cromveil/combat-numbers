package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;

/**
 * Abstracts the mechanism for submitting 3D geometry so that
 * {@code ISkinRenderer} and world-space strategies never import
 * {@code SubmitNodeCollector} directly.
 * <p>
 * Each Minecraft version supplies its own adapter:
 * <ul>
 *   <li>1.21.1 – {@code BufferSourceAdapter} (wrapping
 *       {@code MultiBufferSource.BufferSource})</li>
 * </ul>
 */
public interface IGeometrySubmitter {

	/**
	 * Queue a geometry submission. The {@code renderer} receives the current
	 * pose and a ready-to-use {@code VertexConsumer} targeting the given
	 * render type's buffer.
	 */
	void submitGeometry(PoseStack poseStack, RenderType renderType,
			ICustomGeometryRenderer renderer);
}
