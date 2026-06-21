package cromveil.combatnumbers.events;

import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import java.util.Set;

public interface CombatEvent {
	Identifier kind();
	LivingEntity entity();
	float value();
	Set<Identifier> flags();
	Optional<Identifier> typeKey();

	Identifier DAMAGE_KIND = Identifier.fromNamespaceAndPath("combatnumbers", "damage");
	Identifier HEAL_KIND = Identifier.fromNamespaceAndPath("combatnumbers", "heal");
	Identifier GENERIC_HEAL = Identifier.fromNamespaceAndPath("combatnumbers", "generic_heal");

	record Damage(LivingEntity entity, DamageSource source, float rawDamage, float finalDamage, Set<Identifier> flags) implements CombatEvent {
		@Override public Identifier kind() { return DAMAGE_KIND; }
		@Override public float value() { return finalDamage; }
		@Override public Optional<Identifier> typeKey() {
			return source().typeHolder().unwrapKey().map(k -> k.identifier());
		}
	}

	record Heal(LivingEntity entity, float amount, @Nullable Identifier type, Set<Identifier> flags) implements CombatEvent {
		@Override public Identifier kind() { return HEAL_KIND; }
		@Override public float value() { return amount; }
		@Override public Optional<Identifier> typeKey() {
			return Optional.ofNullable(type);
		}
	}
}
