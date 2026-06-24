package cromveil.combatnumbers.client.animation;

import cromveil.combatnumbers.animation.Channel;

public class ChannelBuffer {
	private final float[] values;

	public ChannelBuffer() {
		this.values = new float[Channel.COUNT];
		reset();
	}

	public float get(Channel ch) {
		return values[ch.ordinal()];
	}

	public void set(Channel ch, float value) {
		values[ch.ordinal()] = value;
	}

	public void reset() {
		System.arraycopy(Channel.DEFAULTS, 0, values, 0, Channel.COUNT);
	}

	public void copyFrom(ChannelBuffer other) {
		System.arraycopy(other.values, 0, values, 0, Channel.COUNT);
	}
}
