package cromveil.combatnumbers.client.render;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Version-agnostic camera data required by the floating-text renderer.
 * <p>
 * Strategies and helpers receive this instead of the version-varying
 * {@code CameraRenderState} so that the core rendering pipeline never
 * imports a Minecraft camera class.
 */
public record RenderCamera(Vec3 position, Matrix4f viewRotation, Matrix4f projection,
		Quaternionf orientation) {
}
