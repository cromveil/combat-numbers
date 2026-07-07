package cromveil.combatnumbers.client.render;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Renders floating text on the HUD. The one that works against all edge cases
 * I know of so far.
 */
public final class HudStrategy implements IStrategy {

	private static final float NDC_MARGIN = 1.1f;

	private final IHudRenderContext ctx;
	private final RenderCamera cam;
	private final int guiWidth;
	private final int guiHeight;

	private float screenX;
	private float screenY;

	public HudStrategy(IHudRenderContext ctx, RenderCamera cam) {
		this.ctx = ctx;
		this.cam = cam;
		this.guiWidth = ctx.guiWidth();
		this.guiHeight = ctx.guiHeight();
	}

	@Override
	public Vec3 camPos() {
		return cam.position();
	}

	@Override
	public boolean cull(FloatingText text) {
		Vec3 worldPos = text.worldPos;
		Vec3 camPos = cam.position();
		Matrix4f projection = cam.projection();
		Matrix4f viewRotation = cam.viewRotation();

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
		float s = placement.scale() * placement.perceivedScale() / BillboardHelper.fontReferenceHeight();
		ctx.pushMatrix();
		ctx.translate(screenX, screenY);
		ctx.translate(placement.offX() * placement.perceivedScale(),
				-placement.offY() * placement.perceivedScale());
		if (placement.rotation() != 0f) {
			ctx.rotate((float) Math.toRadians(placement.rotation()));
		}
		ctx.scale(s, s);
		if (perChar) {
			text.visual.renderChar2d(charIndex, ctx, placement.alpha());
		} else {
			text.visual.render2d(ctx, placement.alpha());
		}
		ctx.popMatrix();
	}
}
