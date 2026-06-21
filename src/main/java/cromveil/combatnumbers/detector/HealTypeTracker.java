package cromveil.combatnumbers.detector;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface HealTypeTracker {
	void combatNumbers$setHealType(@Nullable Identifier type);
	@Nullable Identifier combatNumbers$getAndClearHealType();
}
