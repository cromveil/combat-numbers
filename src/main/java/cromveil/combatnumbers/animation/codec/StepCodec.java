package cromveil.combatnumbers.animation.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import cromveil.combatnumbers.animation.Channel;
import cromveil.combatnumbers.animation.Playback;
import cromveil.combatnumbers.animation.Stagger;
import cromveil.combatnumbers.animation.Step;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class StepCodec {

	private StepCodec() {
	}

	static final Codec<String> START_CODEC = Codec.either(
			Codec.INT,
			Codec.STRING).xmap(
					either -> either.map(Object::toString, s -> s),
					s -> {
						try {
							return Either.left(Integer.parseInt(s));
						} catch (NumberFormatException e) {
							return Either.right(s);
						}
					});

	static final Codec<Step> RAW_CODEC = RecordCodecBuilder.create(inst -> inst.group(
			START_CODEC.optionalFieldOf("start", "+0").forGetter(Step::at),
			Codec.INT.fieldOf("duration").forGetter(Step::durationMs),
			Codec.INT.optionalFieldOf("plays", 1).forGetter(Step::plays),
			Codec.STRING.optionalFieldOf("label").forGetter(s -> Optional.ofNullable(s.label())),
			Stagger.CODEC.optionalFieldOf("stagger").forGetter(s -> Optional.ofNullable(s.stagger())),
			Playback.CODEC.optionalFieldOf("playback", Playback.FORWARD).forGetter(Step::playback),
			Codec.unboundedMap(Channel.CODEC, InterpolatorCodec.INTERPOLATOR_CODEC)
					.optionalFieldOf("channels", Map.of())
					.forGetter(Step::channels))
			.apply(inst, (at, dur, plays, label, stagger, playback, channels) -> {
				var copy = new LinkedHashMap<>(channels);
				return new Step(at, -1, dur, plays, label.orElse(null),
						stagger.orElse(null), playback, Map.copyOf(copy));
			}));

	public static int resolveStart(String raw, int selfIndex, int prevStartMs, int prevEndMs,
			Map<String, Integer> labelTimes) {
		if (raw == null || raw.isEmpty()) {
			return selfIndex == 0 ? 0 : prevEndMs;
		}
		String s = raw.trim();

		if (s.matches("-?\\d+")) {
			return Integer.parseInt(s);
		}

		if (s.equals("<")) {
			if (selfIndex == 0)
				throw new IllegalArgumentException("Cannot use '<' on first step");
			return prevStartMs;
		}

		if (s.startsWith("+") || s.startsWith("-")) {
			return prevEndMs + Integer.parseInt(s);
		}

		int plusIdx = s.indexOf('+');
		int minusIdx = s.indexOf('-', 1);
		int sepIdx = plusIdx > 0 ? plusIdx : (minusIdx > 0 ? minusIdx : -1);
		if (sepIdx > 0) {
			String label = s.substring(0, sepIdx);
			int offset = Integer.parseInt(s.substring(sepIdx));
			Integer labelTime = labelTimes.get(label);
			if (labelTime == null)
				throw new IllegalArgumentException(
						"Unknown label '" + label + "' referenced by '" + raw + "'");
			return labelTime + offset;
		}

		Integer labelTime = labelTimes.get(s);
		if (labelTime != null)
			return labelTime;

		throw new IllegalArgumentException(
				"Invalid start value '" + raw
						+ "'. Expected an integer, '+N', '-N', '<', 'label', or 'label+N'");
	}
}
