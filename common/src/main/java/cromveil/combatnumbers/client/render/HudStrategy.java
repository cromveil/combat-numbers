package cromveil.combatnumbers.client.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Renders floating text on the HUD. The one that works against all edge cases
 * I know of so far.
 */
public final class HudStrategy implements Strategy {

	private static final float NDC_MARGIN = 1.1f;

	private final GuiGraphicsExtractor graphics;
	private final Matrix3x2fStack pose;
	private final CameraRenderState cam;
	private final int guiWidth;
	private final int guiHeight;

	private float screenX;
	private float screenY;

	public HudStrategy(GuiGraphicsExtractor graphics, CameraRenderState cam) {
		this.graphics = graphics;
		this.pose = graphics.pose();
		this.cam = cam;
		this.guiWidth = graphics.guiWidth();
		this.guiHeight = graphics.guiHeight();
	}

	@Override
	public Vec3 camPos() {
		return cam.pos;
	}

	@Override
	public boolean cull(FloatingText text) {
		Vec3 worldPos = text.worldPos;
		Vec3 camPos = cam.pos;
		Matrix4f projection = cam.projectionMatrix;
		Matrix4f viewRotation = cam.viewRotationMatrix;

		Vector4f clip = new Vector4f(
				(float) (worldPos.x - camPos.x),
				(float) (worldPos.y - camPos.y),
				(float) (worldPos.z - camPos.z),
				1.0f);
		viewRotation.transform(clip);
		projection.transform(clip);
		if (clip.w <= 1.0e-4f) {
			return true;
		}

		float ndcX = clip.x / clip.w;
		float ndcY = clip.y / clip.w;
		if (ndcX < -NDC_MARGIN || ndcX > NDC_MARGIN || ndcY < -NDC_MARGIN || ndcY > NDC_MARGIN) {
			return true;
		}

		screenX = (ndcX * 0.5f + 0.5f) * guiWidth;
		screenY = (0.5f - ndcY * 0.5f) * guiHeight;
		return false;
	}

	@Override
	public void draw(FloatingText text, int charIndex, boolean perChar, GlyphPlacement placement) {
		float s = placement.scale() * placement.perceivedScale() / BillboardMath.fontReferenceHeight();
		pose.pushMatrix();
		pose.translate(screenX, screenY);
		pose.translate(placement.offX() * placement.perceivedScale(),
				-placement.offY() * placement.perceivedScale());
		if (placement.rotation() != 0f) {
			pose.rotate((float) Math.toRadians(placement.rotation()));
		}
		pose.scale(s, s);
		if (perChar) {
			text.visual.renderChar2d(charIndex, graphics, placement.alpha());
		} else {
			text.visual.render2d(graphics, placement.alpha());
		}
		pose.popMatrix();
	}
}
