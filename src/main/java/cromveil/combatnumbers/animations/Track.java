package cromveil.combatnumbers.animations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record Track(List<Keyframe> keyframes, List<Modifier> modifiers) {
	public Track {
		keyframes = List.copyOf(keyframes);
		modifiers = List.copyOf(modifiers);
	}

	public float sample(float progress, float randomSample) {
		float value = sampleKeyframes(progress);
		for (var modifier : modifiers) {
			value = modifier.apply(progress, value, randomSample);
		}
		return value;
	}

	private float sampleKeyframes(float progress) {
		if (keyframes.isEmpty()) return 0f;
		if (keyframes.size() == 1) return keyframes.getFirst().value();

		var first = keyframes.getFirst();
		var last = keyframes.getLast();

		if (progress <= first.time()) return first.value();
		if (progress >= last.time()) return last.value();

		for (int i = 0; i < keyframes.size() - 1; i++) {
			var a = keyframes.get(i);
			var b = keyframes.get(i + 1);
			if (progress >= a.time() && progress < b.time()) {
				float segment = (progress - a.time()) / (b.time() - a.time());
				Easing easing = a.easing() != null ? a.easing() : Easing.LINEAR;
				float eased = easing.apply(segment);
				return lerp(a.value(), b.value(), eased);
			}
		}
		return last.value();
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	public static final Codec<Track> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Keyframe.CODEC.listOf().optionalFieldOf("keyframes", List.of()).forGetter(Track::keyframes),
			Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(Track::modifiers)
		).apply(instance, Track::new)
	);
}
