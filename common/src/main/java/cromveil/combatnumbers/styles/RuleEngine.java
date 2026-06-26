package cromveil.combatnumbers.styles;

import cromveil.combatnumbers.events.CombatEvent;
import net.minecraft.resources.Identifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RuleEngine {
	private Map<Identifier, KindState> kindStates = Map.of();

	private record KindState(List<Rule> rules) {
	}

	public void load(Map<Identifier, List<Rule>> rulesByKind) {
		Map<Identifier, KindState> map = new HashMap<>();
		for (var entry : rulesByKind.entrySet()) {
			var rules = List.copyOf(entry.getValue());
			map.put(entry.getKey(), new KindState(rules));
		}
		kindStates = Map.copyOf(map);
	}

	public Style resolve(CombatEvent event) {
		var state = kindStates.get(event.kind());
		if (state == null)
			return new Style(null, null);

		Style info = new Style(null, null);

		var matching = state.rules().stream()
				.filter(r -> r.when().matches(event))
				.sorted(Comparator.comparingInt((Rule r) -> r.when().specificity())
						.thenComparingInt(state.rules()::indexOf))
				.toList();

		for (var rule : matching) {
			info = info.merge(rule.then());
		}

		return info;
	}

	/**
	 * The complete, finite set of skin ids these rules can ever resolve to,
	 * sorted for deterministic ordering. Used to build the style id table that
	 * is synced to clients so we can reference ids by a compact index.
	 */
	public List<Identifier> emittableSkinIds() {
		return kindStates.values().stream()
				.flatMap(s -> s.rules().stream())
				.map(r -> r.then().skinId())
				.filter(Objects::nonNull)
				.distinct()
				.sorted()
				.toList();
	}

	/** @see #emittableSkinIds() */
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
