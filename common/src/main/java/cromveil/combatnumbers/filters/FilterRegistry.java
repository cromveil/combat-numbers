package cromveil.combatnumbers.filters;

import cromveil.combatnumbers.events.CombatEvent;
import cromveil.combatnumbers.styles.WhenCondition;

import java.util.ArrayList;
import java.util.List;

public class FilterRegistry {
	private final List<WhenCondition> filters = new ArrayList<>();

	public void register(WhenCondition condition) {
		filters.add(condition);
	}

	public boolean passes(CombatEvent event) {
		for (var filter : filters) {
			if (filter.matches(event))
				return false;
		}
		return true;
	}

	public void clear() {
		filters.clear();
	}

	public int size() {
		return filters.size();
	}
}
