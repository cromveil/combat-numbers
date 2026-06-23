package cromveil.combatnumbers.client.animation.eval;

import cromveil.combatnumbers.animation.SpringSolver;

public record EvalSpring(float[] resolvedStarts, float to, float velocity, float stiffness,
		float damping, float mass, int durationMs) implements Eval {

	public EvalSpring {
		resolvedStarts = resolvedStarts.clone();
	}

	@Override
	public float sample(int charIdx, float t) {
		float x0 = Float.isNaN(resolvedStarts[charIdx]) ? to : resolvedStarts[charIdx];
		float tau = t * durationMs / 1000f;
		return SpringSolver.evaluateSpring(x0, to, velocity, stiffness, damping, mass, tau);
	}

	@Override
	public float endValue(int charIdx) {
		return to;
	}
}
