package cromveil.combatnumbers.detector;

import cromveil.combatnumbers.core.StableId;
import org.jspecify.annotations.Nullable;

public interface HealTypeTracker {
	void combatNumbers$setHealType(@Nullable StableId type);
	@Nullable StableId combatNumbers$getAndClearHealType();
}
