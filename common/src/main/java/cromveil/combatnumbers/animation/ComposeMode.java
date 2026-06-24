package cromveil.combatnumbers.animation;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum ComposeMode {
	REPLACE,
	ADD,
	MULTIPLY;

	public static ComposeMode byName(String name) {
		try {
			return valueOf(name.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static final Codec<ComposeMode> CODEC = Codec.STRING.xmap(
			name -> {
				ComposeMode m = byName(name);
				if (m == null)
					throw new IllegalArgumentException("Unknown compose mode: " + name);
				return m;
			},
			mode -> mode.name().toLowerCase(Locale.ROOT));
}
