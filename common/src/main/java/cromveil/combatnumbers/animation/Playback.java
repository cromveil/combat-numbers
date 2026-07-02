package cromveil.combatnumbers.animation;

public sealed interface Playback permits Playback.Forward, Playback.Reverse {

	float apply(float t);

	record Forward() implements Playback {
		@Override
		public float apply(float t) {
			return t;
		}
	}

	record Reverse() implements Playback {
		@Override
		public float apply(float t) {
			return 1f - t;
		}
	}

	public static final Playback FORWARD = new Forward();
	public static final Playback REVERSE = new Reverse();

	@SuppressWarnings("unused")
	static com.mojang.serialization.Codec<Playback> CODEC = com.mojang.serialization.Codec.STRING.xmap(
			name -> switch (name) {
				case "reverse" -> REVERSE;
				default -> FORWARD;
			},
			p -> switch (p) {
				case Reverse r -> "reverse";
				default -> "forward";
			});
}
