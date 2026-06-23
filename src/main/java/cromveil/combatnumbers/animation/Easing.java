package cromveil.combatnumbers.animation;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.Locale;

public enum Easing implements EasingFunction {
	LINEAR(t -> t),
	EASE_IN(Easing::easeIn),
	EASE_OUT(Easing::easeOut),
	EASE_IN_OUT(Easing::easeInOut),
	EASE_OUT_CIRC(Easing::easeOutCirc),
	EASE_IN_CIRC(Easing::easeInCirc),
	EASE_IN_OUT_CIRC(Easing::easeInOutCirc),
	EASE_OUT_BACK(Easing::easeOutBack),
	EASE_IN_BACK(Easing::easeInBack),
	EASE_IN_OUT_BACK(Easing::easeInOutBack),
	EASE_OUT_ELASTIC(Easing::easeOutElastic),
	EASE_IN_ELASTIC(Easing::easeInElastic),
	EASE_IN_OUT_ELASTIC(Easing::easeInOutElastic),
	EASE_OUT_BOUNCE(Easing::easeOutBounce),
	EASE_IN_SINE(Easing::easeInSine),
	EASE_OUT_SINE(Easing::easeOutSine),
	EASE_IN_OUT_SINE(Easing::easeInOutSine);

	private final EasingFunction function;

	Easing(EasingFunction function) {
		this.function = function;
	}

	@Override
	public float apply(float t) {
		return function.apply(t);
	}

	public static Easing byName(String name) {
		try {
			return valueOf(name.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static final Codec<Easing> CODEC = Codec.STRING.xmap(
			name -> {
				Easing e = byName(name);
				if (e == null)
					throw new IllegalArgumentException("Unknown easing: " + name);
				return e;
			},
			e -> e.name().toLowerCase(Locale.ROOT));

	public static final Codec<List<Easing>> LIST_CODEC = CODEC.listOf();

	public static List<Easing> expandEasings(List<Easing> easings, int targetSize) {
		if (easings.isEmpty())
			return List.of(LINEAR);
		Easing[] expanded = new Easing[targetSize];
		for (int i = 0; i < targetSize; i++) {
			expanded[i] = easings.get(i % easings.size());
		}
		return List.of(expanded);
	}

	private static float easeIn(float t) {
		return t * t;
	}

	private static float easeOut(float t) {
		return 1f - (1f - t) * (1f - t);
	}

	private static float easeInOut(float t) {
		return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2f) / 2f;
	}

	private static float easeOutCirc(float t) {
		return (float) Math.sqrt(1f - (t - 1f) * (t - 1f));
	}

	private static float easeInCirc(float t) {
		return 1f - (float) Math.sqrt(1f - t * t);
	}

	private static float easeInOutCirc(float t) {
		return t < 0.5f
				? (1f - (float) Math.sqrt(1f - 4f * t * t)) / 2f
				: ((float) Math.sqrt(1f - (-2f * t + 2f) * (-2f * t + 2f)) + 1f) / 2f;
	}

	private static final float BACK_OVERSHOOT = 1.70158f;

	private static float easeOutBack(float t) {
		float c1 = BACK_OVERSHOOT;
		float c3 = c1 + 1f;
		return 1f + c3 * (float) Math.pow(t - 1f, 3f) + c1 * (float) Math.pow(t - 1f, 2f);
	}

	private static float easeInBack(float t) {
		float c1 = BACK_OVERSHOOT;
		float c3 = c1 + 1f;
		return c3 * t * t * t - c1 * t * t;
	}

	private static float easeInOutBack(float t) {
		float c1 = BACK_OVERSHOOT;
		float c2 = c1 * 1.525f;
		return t < 0.5f
				? ((2f * t) * (2f * t) * ((c2 + 1f) * 2f * t - c2)) / 2f
				: ((2f * t - 2f) * (2f * t - 2f) * ((c2 + 1f) * (t * 2f - 2f) + c2) + 2f) / 2f;
	}

	private static float easeOutElastic(float t) {
		if (t == 0f || t == 1f)
			return t;
		return (float) Math.pow(2f, -10f * t) * (float) Math.sin((t * 10f - 0.75f) * (2f * Math.PI) / 3f) + 1f;
	}

	private static float easeInElastic(float t) {
		if (t == 0f || t == 1f)
			return t;
		return -(float) Math.pow(2f, 10f * t - 10f) * (float) Math.sin((t * 10f - 10.75f) * (2f * Math.PI) / 3f);
	}

	private static float easeInOutElastic(float t) {
		if (t == 0f || t == 1f)
			return t;
		if (t < 0.5f)
			return -(float) Math.pow(2f, 20f * t - 10f) * (float) Math.sin((20f * t - 11.125f) * (2f * Math.PI) / 4.5f) / 2f;
		return (float) Math.pow(2f, -20f * t + 10f) * (float) Math.sin((20f * t - 11.125f) * (2f * Math.PI) / 4.5f) / 2f + 1f;
	}

	private static float easeOutBounce(float t) {
		float n1 = 7.5625f;
		float d1 = 2.75f;
		if (t < 1f / d1)
			return n1 * t * t;
		if (t < 2f / d1) {
			float u = t - 1.5f / d1;
			return n1 * u * u + 0.75f;
		}
		if (t < 2.5f / d1) {
			float u = t - 2.25f / d1;
			return n1 * u * u + 0.9375f;
		}
		float u = t - 2.625f / d1;
		return n1 * u * u + 0.984375f;
	}

	private static float easeInSine(float t) {
		return 1f - (float) Math.cos((t * Math.PI) / 2f);
	}

	private static float easeOutSine(float t) {
		return (float) Math.sin((t * Math.PI) / 2f);
	}

	private static float easeInOutSine(float t) {
		return -(float) (Math.cos(Math.PI * t) - 1f) / 2f;
	}
}
