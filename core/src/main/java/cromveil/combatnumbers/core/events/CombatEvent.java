package cromveil.combatnumbers.core.events;

import cromveil.combatnumbers.core.ResourceId;
import java.util.Optional;
import java.util.Set;

public sealed interface CombatEvent {
	ResourceId kind();
	int entityId();
	float value();
	Set<ResourceId> flags();
	Optional<ResourceId> typeKey();

	ResourceId DAMAGE_KIND = ResourceId.of("combatnumbers", "damage");
	ResourceId HEAL_KIND = ResourceId.of("combatnumbers", "heal");
	ResourceId GENERIC_HEAL = ResourceId.of("combatnumbers", "generic_heal");

	record Damage(int entityId, Optional<ResourceId> typeKey, Set<ResourceId> tags,
			float rawDamage, float finalDamage, Set<ResourceId> flags,
			Optional<Integer> attackerEntityId) implements CombatEvent {
		@Override public ResourceId kind() { return DAMAGE_KIND; }
		@Override public float value() { return finalDamage; }
	}

	record Heal(int entityId, float amount, Optional<ResourceId> type,
			Set<ResourceId> flags) implements CombatEvent {
		@Override public ResourceId kind() { return HEAL_KIND; }
		@Override public float value() { return amount; }
		@Override public Optional<ResourceId> typeKey() { return type; }
	}
}
