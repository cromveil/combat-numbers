package cromveil.combatnumbers.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloatingTextManager {

	private static final int MAX_ACTIVE = 256;
	private static final List<FloatingText> active = new ArrayList<>();

	private FloatingTextManager() {
	}

	public static void add(FloatingText text) {
		if (active.size() >= MAX_ACTIVE)
			active.removeFirst();
		active.add(text);
	}

	public static List<FloatingText> getActive() {
		return Collections.unmodifiableList(active);
	}

	public static void cleanupExpired() {
		active.removeIf(FloatingText::isExpired);
	}

	public static void clear() {
		active.clear();
	}
}
