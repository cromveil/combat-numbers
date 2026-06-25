package cromveil.combatnumbers.client.render;

import net.minecraft.world.phys.Vec3;

/**
 * Common abstraction for every floating-text render strategy (world billboard,
 * fixed-depth screen billboard, and 2D HUD overlay).
 * {@link FloatingTextRenderer}
 * drives the shared fade/scale/stagger pipeline and delegates only the two
 * pieces
 * that actually vary between strategies: per-text visibility and glyph
 * emission.
 */
public interface Strategy {

	/**
	 * Camera position used by the shared driver for distance-based fade and
	 * scaling.
	 */
	Vec3 camPos();

	/**
	 * Per-text visibility test. Implementations may stash strategy-specific data
	 * (e.g. projected screen coordinates) as a side effect.
	 *
	 * @return {@code true} to skip this text
	 */
	boolean cull(FloatingText text);

	/** Emit a single glyph (the whole text, or one staggered character). */
	void draw(FloatingText text, int charIndex, boolean perChar, GlyphPlacement placement);

	/** Resolved per-glyph placement produced by the shared driver. */
	record GlyphPlacement(float offX, float offY, float scale, float rotation,
			float perceivedScale, float alpha) {
	}
}
