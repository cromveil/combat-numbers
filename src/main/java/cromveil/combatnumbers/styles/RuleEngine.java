package cromveil.combatnumbers.styles;

import cromveil.combatnumbers.events.CombatEvent;
import net.minecraft.resources.Identifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleEngine {
	private Map<Identifier, KindState> kindStates = Map.of();

	private record KindState(List<Rule> rules) {}

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
			return new Style(null, null, null);

		Style info = new Style(null, null, null);

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
}
