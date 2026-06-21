package cromveil.combatnumbers.animations;

import com.mojang.serialization.Codec;
import cromveil.combatnumbers.CombatNumbers;
import java.util.HashMap;
import java.util.Map;

public final class Easing {
	private final String name;
	private final Function function;

	private Easing(String name, Function function) {
		this.name = name;
		this.function = function;
	}

	@FunctionalInterface
	public interface Function {
		float apply(float t);
	}

	public String name() {
		return name;
	}

	public float apply(float t) {
		return function.apply(t);
	}

	private static final Map<String, Easing> BY_NAME = new HashMap<>();

	public static Easing byName(String name) {
		Easing easing = BY_NAME.get(name);
		if (easing == null) {
			CombatNumbers.LOGGER.warn("Unknown easing '{}', falling back to linear", name);
			return LINEAR;
		}
		return easing;
	}

	public static final Codec<Easing> CODEC = Codec.STRING.xmap(Easing::byName, Easing::name);

	private static Easing register(String name, Function fn) {
		Easing e = new Easing(name, fn);
		BY_NAME.put(name, e);
		return e;
	}

	public static final Easing LINEAR          = register("linear",          t -> t);
	public static final Easing EASE_IN         = register("ease_in",         t -> t * t);
	public static final Easing EASE_OUT        = register("ease_out",        t -> 1f - (1f - t) * (1f - t));
	public static final Easing EASE_IN_OUT     = register("ease_in_out",     t -> t < 0.5f ? 2f * t * t : 1f - (float)Math.pow(-2f * t + 2f, 2) / 2f);
	public static final Easing EASE_OUT_CIRC   = register("ease_out_circ",   t -> { float m = t - 1f; return (float)Math.sqrt(1f - m * m); });
	public static final Easing EASE_IN_CIRC    = register("ease_in_circ",    t -> 1f - (float)Math.sqrt(1f - t * t));
	public static final Easing EASE_OUT_BACK   = register("ease_out_back",   t -> { float c = 1.70158f; float m = t - 1f; return 1f + (c + 1f) * m * m * m + c * m * m; });
	public static final Easing EASE_IN_BACK    = register("ease_in_back",    t -> { float c = 1.70158f; return (c + 1f) * t * t * t - c * t * t; });
	public static final Easing EASE_OUT_ELASTIC = register("ease_out_elastic", t -> {
		float c4 = (float)(2f * Math.PI) / 3f;
		return t == 0f ? 0f : t == 1f ? 1f : (float)(Math.pow(2f, -10f * t) * Math.sin((t * 10f - 0.75f) * c4) + 1f);
	});
	public static final Easing EASE_OUT_BOUNCE = register("ease_out_bounce", t -> {
		float n1 = 7.5625f;
		float d1 = 2.75f;
		if (t < 1f / d1) return n1 * t * t;
		else if (t < 2f / d1) return n1 * (t -= 1.5f / d1) * t + 0.75f;
		else if (t < 2.5f / d1) return n1 * (t -= 2.25f / d1) * t + 0.9375f;
		else return n1 * (t -= 2.625f / d1) * t + 0.984375f;
	});
}
