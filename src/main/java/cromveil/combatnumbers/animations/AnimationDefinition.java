package cromveil.combatnumbers.animations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record AnimationDefinition(int durationMs, Map<String, Track> tracks) {
	public AnimationDefinition {
		tracks = Map.copyOf(tracks);
	}

	public static final Codec<AnimationDefinition> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.INT.fieldOf("duration_ms").forGetter(AnimationDefinition::durationMs),
			Codec.unboundedMap(Codec.STRING, Track.CODEC).fieldOf("tracks").forGetter(AnimationDefinition::tracks)
		).apply(instance, AnimationDefinition::new)
	);
	
	public static AnimationDefinition createDefault() {
		var scaleTrack = new Track(List.of(
			new Keyframe(0f, 0.4f, Easing.EASE_OUT_BACK),
			new Keyframe(0.333f, 1.0f),
			new Keyframe(0.75f, 1.0f),
			new Keyframe(1f, 0.7f)
		), List.of());
		var yTrack = new Track(List.of(
			new Keyframe(0f, 0f, Easing.EASE_OUT_CIRC),
			new Keyframe(1f, 0.8f)
		), List.of());
		var opacityTrack = new Track(List.of(
			new Keyframe(0f, 1f),
			new Keyframe(0.65f, 1f, Easing.EASE_OUT_CIRC),
			new Keyframe(1f, 0f)
		), List.of());
		var randomMod = List.<Modifier>of(new Modifier.Randomize(-0.3f, 0.3f));
		var xTrack = new Track(List.of(
			new Keyframe(0f, 0f, Easing.EASE_OUT_CIRC),
			new Keyframe(1f, 1f)
		), randomMod);
		var zTrack = new Track(List.of(
			new Keyframe(0f, 0f, Easing.EASE_OUT_CIRC),
			new Keyframe(1f, 1f)
		), randomMod);

		return new AnimationDefinition(1200, Map.of(
			"scale", scaleTrack,
			"y", yTrack,
			"opacity", opacityTrack,
			"x", xTrack,
			"z", zTrack
		));
	}
}
