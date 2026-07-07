package cromveil.combatnumbers.core.filters;

import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.core.styles.IConditionMatcher;

import java.util.ArrayList;
import java.util.List;

public class FilterRegistry<C> {
	private final List<IConditionMatcher<C>> filters = new ArrayList<>();

	public void register(IConditionMatcher<C> condition) {
		filters.add(condition);
	}

	public boolean passes(CombatEvent event, C context) {
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
