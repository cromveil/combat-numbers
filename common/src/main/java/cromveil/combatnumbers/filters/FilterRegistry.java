package cromveil.combatnumbers.filters;

import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.styles.WhenCondition;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class FilterRegistry {
	private final List<WhenCondition> filters = new ArrayList<>();

	public void register(WhenCondition condition) {
		filters.add(condition);
	}

	public boolean passes(CombatEvent event, ServerLevel level) {
		for (var filter : filters) {
			if (filter.matches(event, level))
				return false;
		}
		return true;
	}

	public void clear() {
		filters.clear();
	}
}
