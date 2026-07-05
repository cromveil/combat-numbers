package cromveil.combatnumbers.core.events;

import cromveil.combatnumbers.core.StableId;
import java.util.Optional;
import java.util.Set;

public sealed interface CombatEvent {
	StableId kind();
	int entityId();
	float value();
	Set<StableId> flags();
	Optional<StableId> typeKey();

	StableId DAMAGE_KIND = StableId.of("combatnumbers", "damage");
	StableId HEAL_KIND = StableId.of("combatnumbers", "heal");
	StableId GENERIC_HEAL = StableId.of("combatnumbers", "generic_heal");

	record Damage(int entityId, Optional<StableId> typeKey, Set<StableId> tags,
			float rawDamage, float finalDamage, Set<StableId> flags,
			Optional<Integer> attackerEntityId) implements CombatEvent {
		@Override public StableId kind() { return DAMAGE_KIND; }
		@Override public float value() { return finalDamage; }
	}

	record Heal(int entityId, float amount, Optional<StableId> type,
			Set<StableId> flags) implements CombatEvent {
		@Override public StableId kind() { return HEAL_KIND; }
		@Override public float value() { return amount; }
		@Override public Optional<StableId> typeKey() { return type; }
	}
}
