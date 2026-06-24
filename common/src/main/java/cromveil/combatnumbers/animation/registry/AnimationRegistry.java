package cromveil.combatnumbers.animation.registry;

import cromveil.combatnumbers.animation.Timeline;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnimationRegistry {
	private final Map<Identifier, Timeline> registry = new LinkedHashMap<>();
	private final List<Identifier> orderedIds = new ArrayList<>();
	private Runnable onReload = () -> {
	};

	public AnimationRegistry() {
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}

	public void reload(Map<Identifier, Timeline> entries) {
		registry.clear();
		orderedIds.clear();
		for (var entry : entries.entrySet()) {
			registry.put(entry.getKey(), entry.getValue());
			orderedIds.add(entry.getKey());
		}
		onReload.run();
	}

	public void register(Identifier id, Timeline timeline) {
		if (!registry.containsKey(id)) {
			orderedIds.add(id);
		}
		registry.put(id, timeline);
	}

	public int indexOf(Identifier id) {
		return orderedIds.indexOf(id);
	}

	public Timeline getByIndex(int index) {
		if (index < 0 || index >= orderedIds.size())
			return null;
		return registry.get(orderedIds.get(index));
	}

	public void clear() {
		registry.clear();
		orderedIds.clear();
	}

	public Map<Identifier, Timeline> getAll() {
		Map<Identifier, Timeline> ordered = new LinkedHashMap<>();
		for (Identifier id : orderedIds) {
			ordered.put(id, registry.get(id));
		}
		return ordered;
	}

	public int size() {
		return orderedIds.size();
	}
}
