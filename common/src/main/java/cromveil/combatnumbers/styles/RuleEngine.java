package cromveil.combatnumbers.styles;

import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.core.events.CombatEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RuleEngine {
	private Map<ResourceId, KindState> kindStates = Map.of();

	private record KindState(List<Rule> rules) {
	}

	public void load(Map<ResourceId, List<Rule>> rulesByKind) {
		Map<ResourceId, KindState> map = new HashMap<>();
		for (var entry : rulesByKind.entrySet()) {
			var rules = List.copyOf(entry.getValue());
			map.put(entry.getKey(), new KindState(rules));
		}
		kindStates = Map.copyOf(map);
	}

	public Style resolve(CombatEvent event, ServerLevel level) {
		var state = kindStates.get(event.kind());
		if (state == null)
			return new Style(null, null);

		Style info = new Style(null, null);

		var matching = state.rules().stream()
				.filter(r -> r.when().matches(event, level))
				.sorted(Comparator.comparingInt((Rule r) -> r.when().specificity())
						.thenComparingInt(state.rules()::indexOf))
				.toList();

		for (var rule : matching) {
			info = info.merge(rule.then());
		}

		return info;
	}

	public List<Identifier> emittableSkinIds() {
		return kindStates.values().stream()
				.flatMap(s -> s.rules().stream())
				.map(r -> r.then().skinId())
				.filter(Objects::nonNull)
				.distinct()
				.sorted()
				.toList();
	}

	public List<Identifier> emittableAnimationIds() {
		return kindStates.values().stream()
				.flatMap(s -> s.rules().stream())
				.map(r -> r.then().animationId())
				.filter(Objects::nonNull)
				.distinct()
				.sorted()
				.toList();
	}
}
