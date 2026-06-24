package cromveil.combatnumbers.animation;

public sealed interface Interpolator permits ConstantInterpolator, TweenInterpolator,
		KeyframeInterpolator, SpringInterpolator {

	ComposeMode compose();

	float startValue();

	float endValue();

	static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
