package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import cromveil.combatnumbers.events.CombatEvent;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Optional;

public record WhenCondition(
	@Nullable Identifier type,
	List<Identifier> tags,
	List<Identifier> flags,
	@Nullable EntityPredicate attacker,
	@Nullable EntityPredicate target,
	@Nullable ItemPredicate weapon
) {
	public static final Codec<WhenCondition> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Identifier.CODEC.optionalFieldOf("type").forGetter(w -> Optional.ofNullable(w.type)),
			Identifier.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(WhenCondition::tags),
			Identifier.CODEC.listOf().optionalFieldOf("flags", List.of()).forGetter(WhenCondition::flags),
			EntityPredicate.CODEC.optionalFieldOf("attacker").forGetter(w -> Optional.ofNullable(w.attacker)),
			EntityPredicate.CODEC.optionalFieldOf("target").forGetter(w -> Optional.ofNullable(w.target)),
			ItemPredicate.CODEC.optionalFieldOf("weapon").forGetter(w -> Optional.ofNullable(w.weapon))
		).apply(instance, (type, tags, flags, attacker, target, weapon) ->
			new WhenCondition(
				type.orElse(null), tags, flags,
				attacker.orElse(null), target.orElse(null), weapon.orElse(null)))
	);

	public boolean matches(CombatEvent event) {
		if (type != null) {
			if (event.typeKey().isEmpty() || !event.typeKey().get().equals(type))
				return false;
		}

		if (!tags.isEmpty() || weapon != null) {
			if (!(event instanceof CombatEvent.Damage dmg))
				return false;

			for (var tagId : tags) {
				var tagKey = TagKey.create(Registries.DAMAGE_TYPE, tagId);
				if (!dmg.source().is(tagKey))
					return false;
			}

			if (weapon != null) {
				var attackerEntity = dmg.source().getEntity();
				if (!(attackerEntity instanceof LivingEntity living))
					return false;
				if (!weapon.test(living.getMainHandItem()))
					return false;
			}
		}

		for (var flag : flags) {
			if (!event.flags().contains(flag))
				return false;
		}

		if (attacker != null) {
			if (!(event instanceof CombatEvent.Damage dmg))
				return false;
			var attackerEntity = dmg.source().getEntity();
			if (attackerEntity == null)
				return false;
			var level = event.entity().level();
			if (!(level instanceof ServerLevel serverLevel))
				return false;
			if (!attacker.matches(serverLevel, attackerEntity.position(), attackerEntity))
				return false;
		}

		if (target != null) {
			var level = event.entity().level();
			if (!(level instanceof ServerLevel serverLevel))
				return false;
			if (!target.matches(serverLevel, event.entity().position(), event.entity()))
				return false;
		}

		return true;
	}

	public int specificity() {
		int count = 0;
		if (type != null) count++;
		if (!tags.isEmpty()) count++;
		if (!flags.isEmpty()) count++;
		if (attacker != null) count++;
		if (target != null) count++;
		if (weapon != null) count++;
		return count;
	}
}
