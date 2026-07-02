package cromveil.combatnumbers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Shared base for strategies related to world space rendered text.
 * 
 * Both {@link WorldStrategy} and {@link ScreenStrategy} cull against the view
 * frustum and emit glyphs with an identical face-camera / offset / rotate /
 * scale transform; they differ only in how the glyph anchor is placed and which
 * depth drives the GUI-pixel-to-world scale.
 */
public abstract sealed class BillboardStrategy implements Strategy
		permits WorldStrategy, ScreenStrategy {

	protected final PoseStack ps;
	protected final SubmitNodeCollector collector;
	protected final CameraRenderState cam;

	protected BillboardStrategy(PoseStack ps, SubmitNodeCollector collector, CameraRenderState cam) {
		this.ps = ps;
		this.collector = collector;
		this.cam = cam;
	}

	public static BillboardStrategy create(RenderOption option, PoseStack ps,
			SubmitNodeCollector collector, CameraRenderState cam) {
		return option == RenderOption.SCREEN
				? new ScreenStrategy(ps, collector, cam)
				: new WorldStrategy(ps, collector, cam);
	}

	@Override
	public Vec3 camPos() {
		return cam.pos;
	}

	@Override
	public boolean cull(FloatingText text) {
		Vec3 p = text.worldPos;
		return !RenderStateCache.cullFrustum().pointInFrustum(p.x, p.y, p.z);
	}

	@Override
	public void draw(FloatingText text, int charIndex, boolean perChar, GlyphPlacement placement) {
		ps.pushPose();
		float guiPixelToWorld = anchor(text.worldPos);
		BillboardHelper.faceCamera(ps, cam);
		BillboardHelper.translatePixelOffsets(ps, placement.offX(), placement.offY(),
				guiPixelToWorld, placement.perceivedScale());
		BillboardHelper.rotateZ(ps, placement.rotation());
		BillboardHelper.scaleBillboard(ps, placement.scale(), guiPixelToWorld, placement.perceivedScale());
		if (perChar) {
			text.visual.renderChar3d(charIndex, ps, collector, placement.alpha(), LightTexture.FULL_BRIGHT);
		} else {
			text.visual.render3d(ps, collector, placement.alpha(), LightTexture.FULL_BRIGHT);
		}
		ps.popPose();
	}

	/**
	 * Translate the pose to this text's anchor and return the GUI-pixel-to-world
	 * factor used for the subsequent offset and scale steps.
	 */
	protected abstract float anchor(Vec3 worldPos);
}
