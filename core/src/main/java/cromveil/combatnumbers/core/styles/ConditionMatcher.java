package cromveil.combatnumbers.core.styles;

import cromveil.combatnumbers.core.events.CombatEvent;

@FunctionalInterface
public interface ConditionMatcher<C> {
	boolean matches(CombatEvent event, C context);
	default int specificity() { return 0; }
}
