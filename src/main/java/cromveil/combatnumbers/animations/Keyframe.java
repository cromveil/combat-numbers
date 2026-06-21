package cromveil.combatnumbers.animations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record Keyframe(float time, float value, Easing easing) {
	public Keyframe(float time, float value) {
		this(time, value, null);
	}

	public static final Codec<Keyframe> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.FLOAT.fieldOf("time").forGetter(Keyframe::time),
			Codec.FLOAT.fieldOf("value").forGetter(Keyframe::value),
			Easing.CODEC.optionalFieldOf("easing").forGetter(kf -> Optional.ofNullable(kf.easing()))
		).apply(instance, (time, value, easing) -> new Keyframe(time, value, easing.orElse(null)))
	);
}
