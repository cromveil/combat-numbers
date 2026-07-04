package cromveil.combatnumbers.core.filters;

import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.core.styles.ConditionMatcher;

import java.util.ArrayList;
import java.util.List;

public class FilterRegistry {
	private final List<ConditionMatcher> filters = new ArrayList<>();

	public void register(ConditionMatcher condition) {
		filters.add(condition);
	}

	public boolean passes(CombatEvent event, Object context) {
		for (var filter : filters) {
			if (filter.matches(event, context))
				return false;
		}
		return true;
	}

	public void clear() {
		filters.clear();
	}
}
