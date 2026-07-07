package cromveil.combatnumbers.core.animation.runtime.eval;

public sealed interface Eval permits EvalConstant, EvalTween, EvalKeyframe, EvalSpring {

	float sample(int charIdx, float t);

	float endValue(int charIdx);
}
