package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.core.styles.IConditionMatcher;
import cromveil.combatnumbers.StableIdMapper;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Optional;

public record WhenCondition(
	@Nullable ResourceLocation type,
	List<ResourceLocation> tags,
	List<ResourceLocation> flags,
	@Nullable EntityPredicate attacker,
	@Nullable EntityPredicate target,
	@Nullable ItemPredicate weapon
) implements IConditionMatcher<ServerLevel> {
	public static final Codec<WhenCondition> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			ResourceLocation.CODEC.optionalFieldOf("type").forGetter(w -> Optional.ofNullable(w.type)),
			ResourceLocation.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(WhenCondition::tags),
			ResourceLocation.CODEC.listOf().optionalFieldOf("flags", List.of()).forGetter(WhenCondition::flags),
			EntityPredicate.CODEC.optionalFieldOf("attacker").forGetter(w -> Optional.ofNullable(w.attacker)),
			EntityPredicate.CODEC.optionalFieldOf("target").forGetter(w -> Optional.ofNullable(w.target)),
			ItemPredicate.CODEC.optionalFieldOf("weapon").forGetter(w -> Optional.ofNullable(w.weapon))
		).apply(instance, (type, tags, flags, attacker, target, weapon) ->
			new WhenCondition(
				type.orElse(null), tags, flags,
				attacker.orElse(null), target.orElse(null), weapon.orElse(null)))
	);

	@Override
	public boolean matches(CombatEvent event, ServerLevel level) {

		if (type != null) {
			StableId expected = StableIdMapper.from(type);
			if (event.typeKey().isEmpty() || !event.typeKey().get().equals(expected))
				return false;
		}

		if (!tags.isEmpty() || weapon != null) {
			if (!(event instanceof CombatEvent.Damage dmg))
				return false;

			for (ResourceLocation tagId : tags) {
				StableId expectedTag = StableIdMapper.from(tagId);
				if (!dmg.tags().contains(expectedTag))
					return false;
			}

			if (weapon != null) {
				var attackerId = dmg.attackerEntityId();
				if (attackerId.isEmpty())
					return false;
				var entity = level.getEntity(attackerId.get());
				if (!(entity instanceof LivingEntity living))
					return false;
				if (!weapon.test(living.getMainHandItem()))
					return false;
			}
		}

		for (ResourceLocation flag : flags) {
			StableId expectedFlag = StableIdMapper.from(flag);
			if (!event.flags().contains(expectedFlag))
				return false;
		}

		if (attacker != null) {
			if (!(event instanceof CombatEvent.Damage dmg))
				return false;
			var attackerId = dmg.attackerEntityId();
			if (attackerId.isEmpty())
				return false;
			var attackerEntity = level.getEntity(attackerId.get());
			if (attackerEntity == null)
				return false;
			if (!attacker.matches(level, attackerEntity.position(), attackerEntity))
				return false;
		}

		if (target != null) {
			var entity = level.getEntity(event.entityId());
			if (entity == null)
				return false;
			if (!target.matches(level, entity.position(), entity))
				return false;
		}

		return true;
	}

	@Override
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
