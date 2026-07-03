package cromveil.combatnumbers.filters;

import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.styles.WhenCondition;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public class FilterProcessor {
	private final FilterRegistry registry;

	public FilterProcessor(FilterRegistry registry) {
		this.registry = registry;
	}

	public void accept(Map<Identifier, List<WhenCondition>> entries) {
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
