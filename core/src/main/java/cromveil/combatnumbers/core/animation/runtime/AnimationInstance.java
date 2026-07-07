package cromveil.combatnumbers.core.animation.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AnimationInstance {

	private final IAnimationEvaluator evaluator;
	private final List<Consumer<AnimationInstance>> completeListeners = new ArrayList<>();
	private boolean completeFired;

	public AnimationInstance(IAnimationEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public IAnimationEvaluator evaluator() {
		return evaluator;
	}

	public boolean isComplete(float elapsedMs) {
		return evaluator.isComplete(elapsedMs);
	}

	public float durationMs() {
		return evaluator.durationMs();
	}

	public boolean hasStagger() {
		return evaluator.hasStagger();
	}

	public void sample(int charIdx, ChannelBuffer out, float elapsedMs) {
		evaluator.sampleAll(elapsedMs, charIdx, out);
	}

	public void update(float elapsedMs) {
		if (!completeFired && evaluator.isComplete(elapsedMs)) {
			completeFired = true;
			for (var listener : completeListeners) {
				listener.accept(this);
			}
		}
	}

	public void onComplete(Consumer<AnimationInstance> listener) {
		completeListeners.add(listener);
	}
}
