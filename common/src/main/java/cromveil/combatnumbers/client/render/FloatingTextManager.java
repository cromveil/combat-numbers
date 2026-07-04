package cromveil.combatnumbers.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FloatingTextManager {

	private static final int MAX_ACTIVE = 256;
	private final List<FloatingText> active = new ArrayList<>();

	public void add(FloatingText text) {
		if (active.size() >= MAX_ACTIVE)
			active.removeFirst();
		active.add(text);
	}

	public List<FloatingText> getActive() {
		return Collections.unmodifiableList(active);
	}

	public void cleanupExpired() {
		active.removeIf(FloatingText::isExpired);
	}

	public void clear() {
		active.clear();
	}
}
