package cromveil.combatnumbers.client.animation;

import cromveil.combatnumbers.animation.Channel;
import cromveil.combatnumbers.animation.ConstantInterpolator;
import cromveil.combatnumbers.animation.Interpolator;
import cromveil.combatnumbers.animation.KeyframeDef;
import cromveil.combatnumbers.animation.KeyframeInterpolator;
import cromveil.combatnumbers.animation.SpringInterpolator;
import cromveil.combatnumbers.animation.Step;
import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.animation.TweenInterpolator;
import cromveil.combatnumbers.client.animation.eval.Eval;
import cromveil.combatnumbers.client.animation.eval.EvalConstant;
import cromveil.combatnumbers.client.animation.eval.EvalKeyframe;
import cromveil.combatnumbers.client.animation.eval.EvalSpring;
import cromveil.combatnumbers.client.animation.eval.EvalTween;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AnimationCompiler {

	private static final int CACHE_MAX = 64;

	private record CacheKey(int timelineHash, int charCount, long seed) {
	}

	private final Map<CacheKey, AnimationEvaluator> cache = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<CacheKey, AnimationEvaluator> eldest) {
			return size() > CACHE_MAX;
		}
	};

	public AnimationEvaluator compile(Timeline timeline, int charCount, long seed) {
		var key = new CacheKey(timeline.hashCode(), charCount, seed);
		AnimationEvaluator cached = cache.get(key);
		if (cached != null)
			return cached;

		AnimationEvaluator compiled = build(timeline, charCount, seed);
		cache.put(key, compiled);
		return compiled;
	}

	public void clearCache() {
		cache.clear();
	}

	private AnimationEvaluator build(Timeline timeline, int charCount, long seed) {
		List<Step> steps = timeline.steps();
		var sharedRng = new Random(seed);

		float[][] staggerDelays = new float[steps.size()][charCount];
		boolean anyStagger = false;
		for (int si = 0; si < steps.size(); si++) {
			Step step = steps.get(si);
			if (step.hasStagger() && charCount > 0) {
				anyStagger = true;
				float[] delays = step.stagger().computeDelays(charCount);
				System.arraycopy(delays, 0, staggerDelays[si], 0, charCount);
			}
		}

		float[][] carryValues = new float[charCount][Channel.COUNT];
		for (int c = 0; c < charCount; c++) {
			System.arraycopy(Channel.DEFAULTS, 0, carryValues[c], 0, Channel.COUNT);
		}

		List<List<CompiledStep>> channelStepLists = new ArrayList<>(Channel.COUNT);
		for (int p = 0; p < Channel.COUNT; p++) {
			channelStepLists.add(new ArrayList<>());
		}

		for (int si = 0; si < steps.size(); si++) {
			Step step = steps.get(si);
			boolean stepHasStagger = step.hasStagger() && charCount > 0;
			int stepChars = stepHasStagger ? charCount : 1;

			for (var entry : step.channels().entrySet()) {
				Channel ch = entry.getKey();
				Interpolator interpolator = entry.getValue();
				int chIdx = ch.ordinal();

				float[] resolvedStarts = new float[charCount];
				float explicitStart = interpolator.startValue();

				if (!stepHasStagger) {
					float resolved = resolveStart(interpolator, explicitStart,
							carryValues[0][chIdx], sharedRng);
					for (int c = 0; c < charCount; c++) {
						resolvedStarts[c] = resolved;
					}
				} else {
					for (int c = 0; c < charCount; c++) {
						var charRng = new Random(seed ^ c);
						resolvedStarts[c] = resolveStart(interpolator, explicitStart,
								carryValues[c][chIdx], charRng);
					}
				}

				Eval eval = createEval(interpolator, resolvedStarts, step.durationMs(),
						stepChars, charCount, sharedRng, seed);

				CompiledStep compiledStep = new CompiledStep(
						eval, step.startMs(), step.durationMs(), step.plays(),
						interpolator.compose(), step.playback(), staggerDelays[si]);

				channelStepLists.get(chIdx).add(compiledStep);

				for (int c = 0; c < charCount; c++) {
					carryValues[c][chIdx] = eval.endValue(c);
				}
			}
		}

		CompiledStep[][] channelSteps = new CompiledStep[Channel.COUNT][];
		for (int ch = 0; ch < Channel.COUNT; ch++) {
			List<CompiledStep> list = channelStepLists.get(ch);
			channelSteps[ch] = list.toArray(new CompiledStep[0]);
		}

		return new CompiledAnimation(timeline.totalDurationMs(), charCount,
				channelSteps, anyStagger);
	}

	private static Eval createEval(Interpolator ip, float[] resolvedStarts, int durationMs,
			int stepChars, int charCount, Random rng, long seed) {
		return switch (ip) {
			case ConstantInterpolator c -> {
				if (c.source() instanceof ConstantInterpolator.Fixed) {
					yield new EvalConstant(resolvedStarts);
				}
				float[] vals = new float[charCount];
				for (int i = 0; i < charCount; i++) {
					vals[i] = resolvedStarts[i];
				}
				yield new EvalConstant(vals);
			}
			case TweenInterpolator t -> {
				float to = resolveTweenTo(t, rng);
				if (stepChars == 1 && charCount > 1) {
					float[] starts = new float[charCount];
					for (int c = 0; c < charCount; c++) {
						starts[c] = resolvedStarts[0];
					}
					yield new EvalTween(starts, to, t.easing());
				}
				yield new EvalTween(resolvedStarts, to, t.easing());
			}
			case KeyframeInterpolator k -> {
				KeyframeDef[] kfs = k.keyframes().toArray(new KeyframeDef[0]);
				float firstVal = kfs.length > 0 ? kfs[0].value() : 0f;
				float lastVal = kfs.length > 0 ? kfs[kfs.length - 1].value() : 0f;
				if (stepChars == 1 && charCount > 1) {
					float[] starts = new float[charCount];
					for (int c = 0; c < charCount; c++) {
						starts[c] = resolvedStarts[0];
					}
					yield new EvalKeyframe(starts, kfs, k.fallbackEasing(), firstVal, lastVal);
				}
				yield new EvalKeyframe(resolvedStarts, kfs, k.fallbackEasing(), firstVal, lastVal);
			}
			case SpringInterpolator s -> {
				float mass = cromveil.combatnumbers.animation.SpringSolver.computeMass(
						s.damping(), durationMs / 1000f);
				if (stepChars == 1 && charCount > 1) {
					float[] starts = new float[charCount];
					for (int c = 0; c < charCount; c++) {
						starts[c] = resolvedStarts[0];
					}
					yield new EvalSpring(starts, s.to(), s.velocity(),
							s.stiffness(), s.damping(), mass, durationMs);
				}
				yield new EvalSpring(resolvedStarts, s.to(), s.velocity(),
						s.stiffness(), s.damping(), mass, durationMs);
			}
		};
	}

	private static float resolveTweenTo(TweenInterpolator t, Random rng) {
		if (!t.hasNestedInterpolator())
			return t.toValue();
		Interpolator nested = t.toInterpolator();
		if (nested instanceof ConstantInterpolator c) {
			return c.source().resolve(rng);
		}
		return t.toValue();
	}

	private static float resolveStart(Interpolator ip, float explicitStart,
			float carryForward, Random rng) {
		if (ip instanceof ConstantInterpolator c) {
			return c.source().resolve(rng);
		}
		if (Float.isNaN(explicitStart)) {
			return switch (ip.compose()) {
				case ADD -> 0f;
				case MULTIPLY -> 1f;
				default -> carryForward;
			};
		}
		return explicitStart;
	}
}
