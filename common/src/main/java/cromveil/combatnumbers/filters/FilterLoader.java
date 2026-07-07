package cromveil.combatnumbers.filters;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.styles.WhenCondition;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Map;

public class FilterLoader {
	private final FilterRegistry<ServerLevel> registry;

	public FilterLoader(FilterRegistry<ServerLevel> registry) {
		this.registry = registry;
	}

	public void accept(Map<StableId, List<WhenCondition>> entries) {
		registry.clear();
		int count = 0;
		for (var entry : entries.entrySet()) {
			for (var condition : entry.getValue()) {
				registry.register(condition);
				count++;
			}
		}
		Constants.LOG.info("Loaded {} filters across {} files", count, entries.size());
	}
}
