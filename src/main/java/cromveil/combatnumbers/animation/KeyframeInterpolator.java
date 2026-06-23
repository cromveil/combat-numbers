package cromveil.combatnumbers.animation;

import java.util.List;

import static cromveil.combatnumbers.animation.ComposeMode.REPLACE;

public record KeyframeInterpolator(List<KeyframeDef> keyframes, Easing fallbackEasing,
		ComposeMode compose) implements Interpolator {

	public KeyframeInterpolator {
		keyframes = List.copyOf(keyframes);
		if (fallbackEasing == null)
			fallbackEasing = Easing.LINEAR;
		if (compose == null)
			compose = REPLACE;
	}

	public static KeyframeInterpolator fromValues(float[] values, float[] times,
			List<Easing> easings, Easing fallback, ComposeMode compose) {
		if (values.length == 0)
			return new KeyframeInterpolator(List.of(), fallback, compose);
		List<Easing> expandedEasings = Easing.expandEasings(easings, values.length - 1);
		KeyframeDef[] kfs = new KeyframeDef[values.length];
		float[] resolvedTimes = times == null ? uniformTimes(values.length) : times;
		for (int i = 0; i < values.length; i++) {
			Easing e = expandedEasings.get(Math.min(i, expandedEasings.size() - 1));
			kfs[i] = new KeyframeDef(resolvedTimes[i], values[i], e);
		}
		return new KeyframeInterpolator(List.of(kfs), fallback, compose);
	}

	public static KeyframeInterpolator fromValues(float[] values, Easing fallback, ComposeMode compose) {
		return fromValues(values, null, List.of(), fallback, compose);
	}

	private static float[] uniformTimes(int count) {
		if (count <= 1)
			return new float[] { 0f };
		float[] times = new float[count];
		for (int i = 0; i < count; i++) {
			times[i] = (float) i / (count - 1);
		}
		return times;
	}

	@Override
	public float startValue() {
		return keyframes.isEmpty() ? Float.NaN : keyframes.getFirst().value();
	}

	@Override
	public float endValue() {
		return keyframes.isEmpty() ? Float.NaN : keyframes.getLast().value();
	}
}
