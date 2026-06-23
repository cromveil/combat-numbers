package cromveil.combatnumbers.client.animation.eval;

public record EvalConstant(float[] values) implements Eval {

	public EvalConstant {
		values = values.clone();
	}

	@Override
	public float sample(int charIdx, float t) {
		return values[charIdx];
	}

	@Override
	public float endValue(int charIdx) {
		return values[charIdx];
	}
}
