package cromveil.combatnumbers.animation;

public record Stagger(float delayMs, StaggerFrom from, Easing easing) {

	public Stagger {
		if (easing == null)
			easing = Easing.LINEAR;
	}

	public Stagger(float delayMs, StaggerFrom from) {
		this(delayMs, from, Easing.LINEAR);
	}

	public enum StaggerFrom {
		LEFT,
		RIGHT,
		CENTER,
		EDGES
	}

	public float[] computeDelays(int charCount) {
		float[] delays = new float[charCount];
		float maxDistance = charCount - 1;
		for (int i = 0; i < charCount; i++) {
			float rawDistance = switch (from) {
				case LEFT -> i;
				case RIGHT -> maxDistance - i;
				case CENTER -> Math.abs(i - maxDistance / 2f);
				case EDGES -> Math.min(i, charCount - 1 - i);
			};
			float norm = maxDistance > 0 ? rawDistance / maxDistance : 0f;
			float easedNorm = easing.apply(norm);
			float easedDistance = easedNorm * maxDistance;
			delays[i] = easedDistance * delayMs;
		}
		return delays;
	}

	public static final com.mojang.serialization.Codec<Stagger> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder
			.create(instance -> instance.group(
					com.mojang.serialization.Codec.FLOAT.fieldOf("delay_ms").forGetter(Stagger::delayMs),
					com.mojang.serialization.Codec.STRING.xmap(
							s -> StaggerFrom.valueOf(s.toUpperCase()),
							f -> f.name().toLowerCase()).fieldOf("from").forGetter(Stagger::from),
					Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(Stagger::easing))
					.apply(instance, Stagger::new));
}
