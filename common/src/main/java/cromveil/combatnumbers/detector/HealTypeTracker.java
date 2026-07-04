package cromveil.combatnumbers.detector;

import cromveil.combatnumbers.core.ResourceId;
import org.jspecify.annotations.Nullable;

public interface HealTypeTracker {
	void combatNumbers$setHealType(@Nullable ResourceId type);
	@Nullable ResourceId combatNumbers$getAndClearHealType();
}
