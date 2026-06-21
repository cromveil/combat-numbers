package cromveil.combatnumbers.styles;

import cromveil.combatnumbers.CombatNumbers;
import cromveil.combatnumbers.events.CombatEvent;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleLoader extends SimpleJsonResourceReloadListener<RuleSet> {

	private static final FileToIdConverter LISTER = FileToIdConverter.json("styles");

	private final RuleEngine engine;

	public RuleLoader(RuleEngine engine) {
		super(RuleSet.CODEC, LISTER);
		this.engine = engine;
	}

	@Override
	protected void apply(Map<Identifier, RuleSet> entries,
			ResourceManager manager, ProfilerFiller profiler) {
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
		CombatNumbers.LOGGER.info("Loaded {} rules across {} kinds from {} files",
			total, rulesByKind.size(), entries.size());
	}
}
