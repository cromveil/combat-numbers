package cromveil.combatnumbers.skins;

import cromveil.combatnumbers.CombatNumbers;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkinDefinitionRegistry extends SimpleJsonResourceReloadListener<SkinDefinition> {
	private static final FileToIdConverter LISTER = FileToIdConverter.json("skins");
	private final Map<Identifier, SkinDefinition> registry = new LinkedHashMap<>();
	private final List<Identifier> orderedIds = new ArrayList<>();
	private Runnable onReload = () -> {};
	private ResourceManager lastManager;

	public SkinDefinitionRegistry() {
		super(SkinDefinition.CODEC, LISTER);
	}

	@Override
	protected void apply(Map<Identifier, SkinDefinition> entries, ResourceManager manager, ProfilerFiller profiler) {
		lastManager = manager;
		registry.clear();
		orderedIds.clear();
		for (var entry : entries.entrySet()) {
			registry.put(entry.getKey(), entry.getValue());
			orderedIds.add(entry.getKey());
		}
		CombatNumbers.LOGGER.info("Loaded {} damage skin definitions from server data", registry.size());
		onReload.run();
	}

	public ResourceManager getResourceManager() {
		return lastManager;
	}

	public Map<Identifier, SkinDefinition> getAll() {
		Map<Identifier, SkinDefinition> ordered = new LinkedHashMap<>();
		for (Identifier id : orderedIds) {
			ordered.put(id, registry.get(id));
		}
		return ordered;
	}

	public int indexOf(Identifier id) {
		return orderedIds.indexOf(id);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}

	public void clear() {
		registry.clear();
		orderedIds.clear();
	}
}
