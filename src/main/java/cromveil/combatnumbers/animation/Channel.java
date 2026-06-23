package cromveil.combatnumbers.animation;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum Channel {
	SCALE(1f),
	X(0f),
	Y(0f),
	Z(0f),
	OPACITY(1f),
	ROTATION(0f);

	public static final int COUNT = values().length;
	public static final float[] DEFAULTS;

	static {
		Channel[] vals = values();
		DEFAULTS = new float[vals.length];
		for (int i = 0; i < vals.length; i++) {
			DEFAULTS[i] = vals[i].defaultValue;
		}
	}

	private final float defaultValue;

	Channel(float defaultValue) {
		this.defaultValue = defaultValue;
	}

	public float defaultValue() {
		return defaultValue;
	}

	public int index() {
		return ordinal();
	}

	public static Channel byName(String name) {
		try {
			return valueOf(name.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static final Codec<Channel> CODEC = Codec.STRING.xmap(
			name -> {
				Channel ch = byName(name);
				if (ch == null)
					throw new IllegalArgumentException("Unknown channel: " + name);
				return ch;
			},
			ch -> ch.name().toLowerCase(Locale.ROOT));
}
