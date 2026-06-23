package cromveil.combatnumbers.animation;

import java.util.List;
import java.util.Map;

public record Timeline(int totalDurationMs, List<Step> steps) {

	public static final Timeline DEFAULT = new Timeline(1000,
			List.of(new Step("0", 0, 1000, 1, null, null, Playback.FORWARD, Map.of())));

	public Timeline {
		steps = List.copyOf(steps);
	}

	public boolean isInfinite() {
		return totalDurationMs >= Integer.MAX_VALUE;
	}
}
