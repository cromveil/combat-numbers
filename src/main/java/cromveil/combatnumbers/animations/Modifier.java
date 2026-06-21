package cromveil.combatnumbers.animations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public sealed interface Modifier permits Modifier.Randomize, Modifier.Oscillate {
	float apply(float progress, float currentValue, float randomSample);

	record Randomize(float min, float max) implements Modifier {
		public static final MapCodec<Randomize> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
				Codec.FLOAT.fieldOf("min").forGetter(Randomize::min),
				Codec.FLOAT.fieldOf("max").forGetter(Randomize::max)
			).apply(instance, Randomize::new)
		);

		@Override
		public float apply(float progress, float currentValue, float randomSample) {
			return currentValue * randomSample;
		}
	}

	record Oscillate(float amplitude, float frequency, float decayExponent) implements Modifier {
		public static final MapCodec<Oscillate> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
				Codec.FLOAT.fieldOf("amplitude").forGetter(Oscillate::amplitude),
				Codec.FLOAT.fieldOf("frequency").forGetter(Oscillate::frequency),
				Codec.FLOAT.fieldOf("decay_exponent").forGetter(Oscillate::decayExponent)
			).apply(instance, Oscillate::new)
		);

		@Override
		public float apply(float progress, float currentValue, float randomSample) {
			float decay = 1f - (float) Math.pow(progress, decayExponent);
			float oscillation = (float) Math.sin(progress * Math.PI * frequency) * amplitude * decay;
			return currentValue + oscillation;
		}
	}

	Codec<Modifier> CODEC = Codec.STRING.dispatch(
		"type",
		modifier -> switch (modifier) {
			case Randomize r -> "randomize";
			case Oscillate o -> "oscillate";
		},
		type -> switch (type) {
			case "randomize" -> Randomize.CODEC;
			case "oscillate" -> Oscillate.CODEC;
			default -> throw new IllegalArgumentException("Unknown modifier type: " + type);
		}
	);
}
