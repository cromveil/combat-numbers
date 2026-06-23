package cromveil.combatnumbers.client.animation.eval;

import cromveil.combatnumbers.animation.Easing;
import cromveil.combatnumbers.animation.Interpolator;
import cromveil.combatnumbers.animation.KeyframeDef;

public record EvalKeyframe(float[] resolvedStarts, KeyframeDef[] sortedKeyframes,
		Easing fallbackEasing, float firstValue, float lastValue) implements Eval {

	public EvalKeyframe {
		resolvedStarts = resolvedStarts.clone();
	}

	@Override
	public float sample(int charIdx, float t) {
		if (sortedKeyframes.length == 0)
			return 0f;
		if (sortedKeyframes.length == 1) {
			return Float.isNaN(resolvedStarts[charIdx]) ? sortedKeyframes[0].value() : resolvedStarts[charIdx];
		}

		float startVal = Float.isNaN(resolvedStarts[charIdx]) ? firstValue : resolvedStarts[charIdx];

		if (t <= sortedKeyframes[0].time())
			return startVal;
		if (t >= sortedKeyframes[sortedKeyframes.length - 1].time())
			return lastValue;

		int idx = findSegment(t);
		var a = sortedKeyframes[idx];
		var b = sortedKeyframes[idx + 1];
		float seg = (t - a.time()) / (b.time() - a.time());
		Easing e = a.easing() != Easing.LINEAR ? a.easing() : fallbackEasing;
		float from = (idx == 0) ? startVal : a.value();
		return Interpolator.lerp(from, b.value(), e.apply(seg));
	}

	@Override
	public float endValue(int charIdx) {
		return sortedKeyframes.length == 0 ? 0f : lastValue;
	}

	private int findSegment(float t) {
		int lo = 0;
		int hi = sortedKeyframes.length - 2;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			if (t >= sortedKeyframes[mid + 1].time()) {
				lo = mid + 1;
			} else if (t < sortedKeyframes[mid].time()) {
				hi = mid - 1;
			} else {
				return mid;
			}
		}
		return Math.min(lo, sortedKeyframes.length - 2);
	}
}
