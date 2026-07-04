package cromveil.combatnumbers.core.client.animation.eval;

import cromveil.combatnumbers.core.animation.Easing;
import cromveil.combatnumbers.core.animation.Interpolator;

public record EvalTween(float[] resolvedStarts, float to, Easing easing) implements Eval {

	public EvalTween {
		resolvedStarts = resolvedStarts.clone();
	}

	@Override
	public float sample(int charIdx, float t) {
		return Interpolator.lerp(resolvedStarts[charIdx], to, easing.apply(t));
	}

	@Override
	public float endValue(int charIdx) {
		return to;
	}
}
