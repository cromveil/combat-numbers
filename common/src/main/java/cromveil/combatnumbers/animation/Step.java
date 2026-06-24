package cromveil.combatnumbers.animation;

import java.util.Map;

public record Step(String at, int startMs, int durationMs, int plays, String label,
		Stagger stagger, Playback playback, Map<Channel, Interpolator> channels) {

	public Step {
		channels = Map.copyOf(channels);
		if (playback == null)
			playback = Playback.FORWARD;
	}

	public boolean hasStagger() {
		return stagger != null;
	}

	public Interpolator channel(Channel ch) {
		return channels.get(ch);
	}

	public boolean hasChannel(Channel ch) {
		return channel(ch) != null;
	}
}
