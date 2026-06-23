package cromveil.combatnumbers.animation.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import cromveil.combatnumbers.animation.Step;
import cromveil.combatnumbers.animation.Timeline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TimelineCodec {

	private TimelineCodec() {
	}

	public static final Codec<Timeline> CODEC = StepCodec.RAW_CODEC.listOf().comapFlatMap(
			rawSteps -> {
				List<Step> resolved = new ArrayList<>(rawSteps.size());
				Map<String, Integer> labelTimes = new LinkedHashMap<>();
				int prevStartMs = 0;
				int prevEndMs = 0;
				int maxDuration = 0;
				int infiniteCount = 0;

				for (int i = 0; i < rawSteps.size(); i++) {
					final int stepIdx = i;
					Step raw = rawSteps.get(stepIdx);

					int startMs;
					try {
						startMs = StepCodec.resolveStart(raw.at(), stepIdx, prevStartMs, prevEndMs, labelTimes);
					} catch (IllegalArgumentException e) {
						return DataResult.error(() -> "Step " + stepIdx + ": " + e.getMessage());
					}

					if (raw.durationMs() < 0) {
						return DataResult.error(
								() -> "Step " + stepIdx + ": duration must be >= 0, got " + raw.durationMs());
					}

					if (raw.plays() < -1 || raw.plays() == 0) {
						return DataResult.error(
								() -> "Step " + stepIdx + ": plays must be >= 1 or -1 (infinite), got " + raw.plays());
					}

					Step step = new Step(raw.at(), startMs, raw.durationMs(), raw.plays(),
							raw.label(), raw.stagger(), raw.playback(), raw.channels());
					resolved.add(step);

					if (raw.label() != null) {
						if (labelTimes.containsKey(raw.label())) {
							return DataResult.error(
									() -> "Step " + stepIdx + ": duplicate label '" + raw.label() + "'");
						}
						labelTimes.put(raw.label(), startMs);
					}

					if (raw.plays() == -1) {
						infiniteCount++;
						prevEndMs = startMs + raw.durationMs();
					} else {
						int stepEnd = startMs + raw.durationMs() * raw.plays();
						if (stepEnd > maxDuration)
							maxDuration = stepEnd;
						prevEndMs = stepEnd;
					}
					prevStartMs = startMs;
				}

				if (infiniteCount == rawSteps.size())
					maxDuration = Integer.MAX_VALUE;

				return DataResult.success(new Timeline(maxDuration, List.copyOf(resolved)));
			},
			Timeline::steps);
}
