package cromveil.combatnumbers.core.styles;

import cromveil.combatnumbers.core.events.CombatEvent;

@FunctionalInterface
public interface ConditionMatcher {
	boolean matches(CombatEvent event, Object context);
	default int specificity() { return 0; }
}
