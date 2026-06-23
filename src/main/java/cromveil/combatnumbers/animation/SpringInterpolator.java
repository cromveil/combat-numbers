package cromveil.combatnumbers.animation;

import static cromveil.combatnumbers.animation.ComposeMode.REPLACE;

public record SpringInterpolator(float from, float to, float velocity, float stiffness, float damping,
		ComposeMode compose) implements Interpolator {

	public SpringInterpolator {
		if (stiffness <= 0f)
			stiffness = SpringSolver.DEFAULT_STIFFNESS;
		if (damping <= 0f)
			damping = SpringSolver.DEFAULT_DAMPING;
		if (compose == null)
			compose = REPLACE;
	}

	public static SpringInterpolator withDefaults(float from, float to, float velocity, ComposeMode compose) {
		return new SpringInterpolator(from, to, velocity,
				SpringSolver.DEFAULT_STIFFNESS, SpringSolver.DEFAULT_DAMPING, compose);
	}

	public static SpringInterpolator withDefaults(float from, float to, float velocity) {
		return withDefaults(from, to, velocity, REPLACE);
	}

	@Override
	public float startValue() {
		return Float.isNaN(from) ? Float.NaN : from;
	}

	@Override
	public float endValue() {
		return to;
	}
}
