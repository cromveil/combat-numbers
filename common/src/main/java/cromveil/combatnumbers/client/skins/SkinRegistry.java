package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.skins.SkinDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class SkinRegistry {
	private final Map<Identifier, Skin> registry = new LinkedHashMap<>();
	private final List<Identifier> orderedIds = new ArrayList<>();

	public void register(Identifier id, Skin skin) {
		if (!registry.containsKey(id)) {
			orderedIds.add(id);
		}
		registry.put(id, skin);
	}

	/** Accepts invalid indices, will just return null. */
	public Skin getByIndex(int index) {
		if (index < 0 || index >= orderedIds.size()) return null;
		return registry.get(orderedIds.get(index));
	}

	public void clear() {
		registry.clear();
		orderedIds.clear();
	}

	public int size() {
		return orderedIds.size();
	}

	public void reload(Map<Identifier, SkinDefinition> defs, ResourceManager manager) {
		clear();
		for (var entry : defs.entrySet()) {
			try {
				register(entry.getKey(), SkinFactory.create(entry.getValue(), manager));
			} catch (Exception e) {
				Constants.LOG.warn("Failed to load skin '{}': {}", entry.getKey(), e.getMessage());
			}
		}
	}

	public void reloadFromServer(Map<Identifier, SkinDefinition> serverDefs, ResourceManager manager) {
		var existing = new LinkedHashMap<>(registry);
		clear();
		for (var entry : serverDefs.entrySet()) {
			Identifier id = entry.getKey();
			Skin skin = existing.remove(id);
			if (skin == null) {
				try {
					skin = SkinFactory.create(entry.getValue(), manager);
				} catch (Exception e) {
					Constants.LOG.warn("Failed to create synced skin '{}': {}", id, e.getMessage());
					continue;
				}
			}
			register(id, skin);
		}
		for (var entry : existing.entrySet()) {
			register(entry.getKey(), entry.getValue());
		}
	}
}
