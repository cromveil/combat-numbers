package cromveil.combatnumbers.client.animation;

import cromveil.combatnumbers.animation.Channel;

public class CompiledAnimation implements AnimationEvaluator {

	private final float totalDurationMs;
	private final CompiledStep[][] channelSteps;
	private final int charCount;
	private final boolean hasStagger;

	public CompiledAnimation(float totalDurationMs, int charCount,
			CompiledStep[][] channelSteps, boolean hasStagger) {
		this.totalDurationMs = totalDurationMs;
		this.charCount = charCount;
		this.channelSteps = channelSteps;
		this.hasStagger = hasStagger;
	}

	@Override
	public void sampleAll(float elapsedMs, int charIdx, ChannelBuffer out) {
		if (charIdx < 0 || charIdx >= charCount) {
			out.reset();
			return;
		}

		out.reset();

		for (Channel ch : Channel.values()) {
			CompiledStep[] steps = channelSteps[ch.ordinal()];
			if (steps == null || steps.length == 0)
				continue;

			float acc = ch.defaultValue();

			for (CompiledStep step : steps) {
				float val = step.evaluate(charIdx, elapsedMs);
				if (Float.isNaN(val))
					continue;

				acc = switch (step.compose()) {
					case ADD -> acc + val;
					case MULTIPLY -> acc * val;
					default -> val;
				};
			}

			out.set(ch, acc);
		}
	}

	@Override
	public boolean isComplete(float elapsedMs) {
		if (totalDurationMs <= 0f)
			return true;
		if (totalDurationMs >= (float) Integer.MAX_VALUE)
			return false;
		return elapsedMs >= totalDurationMs;
	}

	@Override
	public float durationMs() {
		return totalDurationMs;
	}

	@Override
	public boolean hasStagger() {
		return hasStagger;
	}
}
