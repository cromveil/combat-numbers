package cromveil.combatnumbers.client.render;

import cromveil.combatnumbers.animations.AnimationDefinition;
import cromveil.combatnumbers.animations.Modifier;
import cromveil.combatnumbers.client.skins.SkinRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;

public class Instance {
	private static final RandomSource SHARED_RANDOM = RandomSource.createThreadLocalInstance();

	public final Vec3 worldPos;
	public final String formattedText;
	public final SkinRenderer visual;
	public final AnimationDefinition animation;
	public final float scale;
	private final double createdAtGameTime;
	private final Map<String, Float> randomSamples = new HashMap<>();

	public Instance(Vec3 worldPos, String formattedText, SkinRenderer visual, AnimationDefinition animation, float scale, double gameTime) {
		this.worldPos = worldPos;
		this.formattedText = formattedText;
		this.visual = visual;
		this.animation = animation;
		this.scale = scale;
		this.createdAtGameTime = gameTime;

		for (var entry : animation.tracks().entrySet()) {
			String trackName = entry.getKey();
			for (var modifier : entry.getValue().modifiers()) {
				if (modifier instanceof Modifier.Randomize r) {
					randomSamples.put(trackName, r.min() + SHARED_RANDOM.nextFloat() * (r.max() - r.min()));
					break;
				}
			}
		}
	}

	private float getProgress(double currentGameTime) {
		double elapsedTicks = currentGameTime - createdAtGameTime;
		return Math.clamp((float) (elapsedTicks * 50.0 / animation.durationMs()), 0f, 1f);
	}

	private boolean hasTrack(String name) {
		return animation.tracks().containsKey(name);
	}

	public float getTrackValue(String trackName, double currentGameTime) {
		var track = animation.tracks().get(trackName);
		if (track == null) return 0f;
		return track.sample(getProgress(currentGameTime), randomSamples.getOrDefault(trackName, 0f));
	}

	public float getAlpha(double currentGameTime) {
		return hasTrack("opacity") ? Math.clamp(getTrackValue("opacity", currentGameTime), 0f, 1f) : 1f;
	}

	public float getScaleMultiplier(double currentGameTime) {
		return hasTrack("scale") ? getTrackValue("scale", currentGameTime) : 1f;
	}

	public Vec3 getMovementOffset(double currentGameTime) {
		return new Vec3(
			getTrackValue("x", currentGameTime),
			getTrackValue("y", currentGameTime),
			getTrackValue("z", currentGameTime)
		);
	}

	public float getRotation(double currentGameTime) {
		return getTrackValue("rotation", currentGameTime);
	}

	public Vec3 getRenderPos(double currentGameTime) {
		return worldPos.add(getMovementOffset(currentGameTime));
	}

	public boolean isExpired(double currentGameTime) {
		return getProgress(currentGameTime) >= 1f;
	}
}
