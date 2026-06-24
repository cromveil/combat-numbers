package cromveil.combatnumbers.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface ValueFormatter {
	String format(float value);

	ValueFormatter DEFAULT = v -> String.valueOf(Math.round(v));

	Map<Identifier, ValueFormatter> REGISTRY = new LinkedHashMap<>();

	static void register(Identifier id, ValueFormatter formatter) {
		REGISTRY.put(id, formatter);
	}

	static String formatValue(float value) {
		if (REGISTRY.isEmpty())
			return DEFAULT.format(value);
		String result = null;
		for (var formatter : REGISTRY.values()) {
			result = formatter.format(value);
		}
		return result != null ? result : DEFAULT.format(value);
	}
}
