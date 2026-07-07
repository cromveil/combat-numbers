package cromveil.combatnumbers.core.animation.runtime;

public interface IAnimationEvaluator {

	void sampleAll(float elapsedMs, int charIdx, ChannelBuffer out);

	boolean isComplete(float elapsedMs);

	float durationMs();

	boolean hasStagger();
}
