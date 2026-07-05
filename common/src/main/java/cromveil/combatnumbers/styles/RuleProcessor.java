package cromveil.combatnumbers.styles;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.core.styles.RuleEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleProcessor {

	private final RuleEngine engine;
	private Runnable onReload = () -> {
	};

	public RuleProcessor(RuleEngine engine) {
		this.engine = engine;
	}

	public void setOnReload(Runnable onReload) {
		this.onReload = onReload;
	}

	public void accept(Map<StableId, RuleSet> entries) {
		Map<StableId, List<RuleEngine.Rule>> rulesByKind = new HashMap<>();

		for (var entry : entries.entrySet()) {
			var path = entry.getKey().path();
			var slashIdx = path.indexOf('/');
			StableId kind;
			if (slashIdx >= 0) {
				kind = StableId.of(
						entry.getKey().namespace(), path.substring(0, slashIdx));
			} else {
				kind = CombatEvent.DAMAGE_KIND;
			}

			var set = entry.getValue();
			var coreRules = set.rules().stream()
					.map(r -> new RuleEngine.Rule(r.when(), r.then()))
					.toList();
			rulesByKind.computeIfAbsent(kind, k -> new ArrayList<>()).addAll(coreRules);
		}

		engine.load(rulesByKind);

		int total = rulesByKind.values().stream().mapToInt(List::size).sum();
		Constants.LOG.info("Loaded {} rules across {} kinds from {} files",
				total, rulesByKind.size(), entries.size());

		onReload.run();
	}
}
