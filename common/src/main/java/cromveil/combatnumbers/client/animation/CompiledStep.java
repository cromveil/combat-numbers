package cromveil.combatnumbers.client.animation;

import cromveil.combatnumbers.animation.ComposeMode;
import cromveil.combatnumbers.animation.Playback;
import cromveil.combatnumbers.client.animation.eval.Eval;

public record CompiledStep(Eval eval, float offsetMs, float durationMs, int plays,
		ComposeMode compose, Playback playback, float[] staggerDelays) {

	public float evaluate(int charIdx, float elapsedMs) {
		float staggeredMs = elapsedMs - staggerDelays[charIdx];
		if (staggeredMs < offsetMs)
			return Float.NaN;

		float localMs = staggeredMs - offsetMs;

		if (durationMs <= 0f)
			return eval.sample(charIdx, playback.apply(1f));

		if (plays >= 0 && localMs >= (float) durationMs * plays)
			return eval.endValue(charIdx);

		float t = (localMs % durationMs) / durationMs;
		return eval.sample(charIdx, playback.apply(t));
	}
}
