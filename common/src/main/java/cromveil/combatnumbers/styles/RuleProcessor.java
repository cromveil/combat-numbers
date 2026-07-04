package cromveil.combatnumbers.styles;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.events.CombatEvent;
import net.minecraft.resources.Identifier;

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

	public void accept(Map<Identifier, RuleSet> entries) {
		Map<Identifier, List<Rule>> rulesByKind = new HashMap<>();

		for (var entry : entries.entrySet()) {
			var path = entry.getKey().getPath();
			var slashIdx = path.indexOf('/');
			Identifier kind;
			if (slashIdx >= 0) {
				kind = Identifier.fromNamespaceAndPath(
						entry.getKey().getNamespace(), path.substring(0, slashIdx));
			} else {
				kind = CombatEvent.DAMAGE_KIND;
			}

			var set = entry.getValue();
			rulesByKind.computeIfAbsent(kind, k -> new ArrayList<>()).addAll(set.rules());
		}

		engine.load(rulesByKind);

		int total = rulesByKind.values().stream().mapToInt(List::size).sum();
		Constants.LOG.info("Loaded {} rules across {} kinds from {} files",
				total, rulesByKind.size(), entries.size());

		onReload.run();
	}
}
