package cromveil.combatnumbers.animation;

import static cromveil.combatnumbers.animation.ComposeMode.REPLACE;

public record TweenInterpolator(float from, Interpolator toInterpolator, float toValue,
		Easing easing, ComposeMode compose) implements Interpolator {

	public TweenInterpolator {
		if (easing == null)
			easing = Easing.LINEAR;
		if (compose == null)
			compose = REPLACE;
	}

	public static TweenInterpolator toValue(float from, float to, Easing easing, ComposeMode compose) {
		return new TweenInterpolator(from, null, to, easing, compose);
	}

	public static TweenInterpolator toValue(float from, float to, Easing easing) {
		return toValue(from, to, easing, REPLACE);
	}

	public static TweenInterpolator toValue(float from, float to) {
		return toValue(from, to, Easing.LINEAR);
	}

	public static TweenInterpolator toInterpolator(float from, Interpolator target, Easing easing, ComposeMode compose) {
		return new TweenInterpolator(from, target, 0f, easing, compose);
	}

	public static TweenInterpolator toInterpolator(float from, Interpolator target, Easing easing) {
		return toInterpolator(from, target, easing, REPLACE);
	}

	public boolean hasNestedInterpolator() {
		return toInterpolator != null;
	}

	@Override
	public float startValue() {
		return Float.isNaN(from) ? Float.NaN : from;
	}

	@Override
	public float endValue() {
		if (toInterpolator != null)
			return toInterpolator.endValue();
		return toValue;
	}
}
