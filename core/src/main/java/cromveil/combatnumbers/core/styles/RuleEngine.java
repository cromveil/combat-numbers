package cromveil.combatnumbers.core.styles;

import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.events.CombatEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class RuleEngine {

	public record Rule(ConditionMatcher when, Style then) {}

	private Map<StableId, KindState> kindStates = Map.of();

	private record KindState(List<Rule> rules) {}

	public void load(Map<StableId, List<Rule>> rulesByKind) {
		Map<StableId, KindState> map = new HashMap<>();
		for (var entry : rulesByKind.entrySet()) {
			map.put(entry.getKey(), new KindState(List.copyOf(entry.getValue())));
		}
		kindStates = Map.copyOf(map);
	}

	public Style resolve(CombatEvent event, Object context) {
		var state = kindStates.get(event.kind());
		if (state == null)
			return new Style(null, null);

		Style info = new Style(null, null);

		var matching = state.rules().stream()
				.filter(r -> r.when().matches(event, context))
				.sorted(Comparator.comparingInt((Rule r) -> r.when().specificity())
						.thenComparingInt(state.rules()::indexOf))
				.toList();

		for (var rule : matching) {
			info = info.merge(rule.then());
		}

		return info;
	}

	public List<StableId> emittableSkinIds() {
		return kindStates.values().stream()
				.flatMap(s -> s.rules().stream())
				.map(r -> r.then().skinId())
				.filter(Objects::nonNull)
				.distinct()
				.sorted()
				.toList();
	}

	public List<StableId> emittableAnimationIds() {
		return kindStates.values().stream()
				.flatMap(s -> s.rules().stream())
				.map(r -> r.then().animationId())
				.filter(Objects::nonNull)
				.distinct()
				.sorted()
				.toList();
	}
}
