package cromveil.combatnumbers.client.animation;

public interface AnimationEvaluator {

	void sampleAll(float elapsedMs, int charIdx, ChannelBuffer out);

	boolean isComplete(float elapsedMs);

	float durationMs();

	boolean hasStagger();
}
