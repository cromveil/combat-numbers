package cromveil.combatnumbers.client.animation.eval;

public sealed interface Eval permits EvalConstant, EvalTween, EvalKeyframe, EvalSpring {

	float sample(int charIdx, float t);

	float endValue(int charIdx);
}
