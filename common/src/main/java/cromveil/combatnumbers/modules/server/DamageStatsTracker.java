package cromveil.combatnumbers.modules.server;

import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.core.events.CombatNumbersEvents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class DamageStatsTracker implements IServerSetup {

	private static final StableId NO_TYPE = StableId.of("combatnumbers", "none");
	private static final StableId NO_TAG = StableId.of("combatnumbers", "none");
	private static final String NO_SOURCE = "<no source>";

	private final EntityLevelResolver entities;
	private final List<DamageEvent> events = new ArrayList<>();

	public DamageStatsTracker(EntityLevelResolver entities) {
		this.entities = entities;
	}

	@Override
	public void register() {
		CombatNumbersEvents.COMBAT.register(this::onCombat);
	}

	private void onCombat(CombatEvent event) {
		if (!(event instanceof CombatEvent.Damage damage))
			return;

		StableId typeKey = damage.typeKey().orElse(NO_TYPE);
		String sourceKey = resolveSource(damage.attackerEntityId());
		Set<StableId> tags = damage.tags().isEmpty()
				? Set.of(NO_TAG)
				: Set.copyOf(damage.tags());

		events.add(new DamageEvent(typeKey, tags, sourceKey));
	}

	private String resolveSource(Optional<Integer> attackerEntityId) {
		if (attackerEntityId.isEmpty())
			return NO_SOURCE;
		ServerLevel level = entities.resolve(attackerEntityId.get());
		if (level == null)
			return NO_SOURCE;
		Entity entity = level.getEntity(attackerEntityId.get());
		if (entity == null)
			return NO_SOURCE;
		if (entity instanceof Player player)
			return player.getGameProfile().getName();
		ResourceLocation typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		if (typeKey != null)
			return typeKey.toString();
		return entity.getType().getDescription().getString();
	}

	public StatsSnapshot getSnapshot(Optional<StableId> typeFilter,
			Optional<StableId> tagFilter, Optional<String> sourceFilter) {
		Map<StableId, Integer> typeCounts = new LinkedHashMap<>();
		Map<StableId, Integer> tagCounts = new LinkedHashMap<>();
		Map<String, Integer> sourceCounts = new LinkedHashMap<>();

		for (DamageEvent e : events) {
			if (typeFilter.isPresent() && !e.typeKey.equals(typeFilter.get()))
				continue;
			if (tagFilter.isPresent() && !e.tags.contains(tagFilter.get()))
				continue;
			if (sourceFilter.isPresent() && !e.sourceKey.equals(sourceFilter.get()))
				continue;

			typeCounts.merge(e.typeKey, 1, Integer::sum);
			for (StableId tag : e.tags) {
				if (!tag.equals(NO_TAG))
					tagCounts.merge(tag, 1, Integer::sum);
			}
			sourceCounts.merge(e.sourceKey, 1, Integer::sum);
		}

		typeCounts.remove(NO_TYPE);
		tagCounts.remove(NO_TAG);
		sourceCounts.remove(NO_SOURCE);

		return new StatsSnapshot(
				sortEntries(typeCounts),
				sortEntries(tagCounts),
				sortSourceEntries(sourceCounts));
	}

	private static List<StatsSnapshot.Entry> sortEntries(Map<StableId, Integer> counts) {
		return counts.entrySet().stream()
				.sorted(Map.Entry.<StableId, Integer>comparingByValue().reversed())
				.map(e -> new StatsSnapshot.Entry(e.getKey(), e.getValue()))
				.toList();
	}

	private static List<StatsSnapshot.SourceEntry> sortSourceEntries(Map<String, Integer> counts) {
		return counts.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.map(e -> new StatsSnapshot.SourceEntry(e.getKey(), e.getValue()))
				.toList();
	}

	private record DamageEvent(StableId typeKey, Set<StableId> tags, String sourceKey) {}
}
