package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.CombatNumbers;
import cromveil.combatnumbers.skins.SkinDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class SkinRegistry {
	private final Map<Identifier, Skin> REGISTRY = new LinkedHashMap<>();
	private final List<Identifier> ORDERED_IDS = new ArrayList<>();

	public void register(Identifier id, Skin skin) {
		if (!REGISTRY.containsKey(id)) {
			ORDERED_IDS.add(id);
		}
		REGISTRY.put(id, skin);
	}

	public Skin getByIndex(int index) {
		return REGISTRY.get(ORDERED_IDS.get(index));
	}

	public void clear() {
		REGISTRY.clear();
		ORDERED_IDS.clear();
	}

	public int size() {
		return ORDERED_IDS.size();
	}

	public void reload(Map<Identifier, SkinDefinition> defs, ResourceManager manager) {
		clear();
		for (var entry : defs.entrySet()) {
			try {
				register(entry.getKey(), SkinFactory.create(entry.getValue(), manager));
			} catch (Exception e) {
				CombatNumbers.LOGGER.warn("Failed to load skin '{}': {}", entry.getKey(), e.getMessage());
			}
		}
	}

	public void reloadFromServer(Map<Identifier, SkinDefinition> serverDefs, ResourceManager manager) {
		var existing = new LinkedHashMap<>(REGISTRY);
		clear();
		for (var entry : serverDefs.entrySet()) {
			Identifier id = entry.getKey();
			Skin skin = existing.remove(id);
			if (skin == null) {
				try {
					skin = SkinFactory.create(entry.getValue(), manager);
				} catch (Exception e) {
					CombatNumbers.LOGGER.warn("Failed to create synced skin '{}': {}", id, e.getMessage());
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
