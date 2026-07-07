package cromveil.combatnumbers.server;

import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.Setup;
import cromveil.combatnumbers.core.config.ConfigState;
import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.core.events.CombatNumbersEvents;
import cromveil.combatnumbers.core.events.DispatchEvent;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.Style;
import net.minecraft.server.level.ServerLevel;

public final class StylingModule implements Setup {

	private final ConfigState config;
	private final RuleEngine<ServerLevel> ruleEngine;
	private final FilterRegistry<ServerLevel> filterRegistry;
	private final EntityLevelResolver entities;
	public StylingModule(ConfigState config,
			RuleEngine<ServerLevel> ruleEngine,
			FilterRegistry<ServerLevel> filterRegistry,
			EntityLevelResolver entities) {
		this.config = config;
		this.ruleEngine = ruleEngine;
		this.filterRegistry = filterRegistry;
		this.entities = entities;
	}

	@Override
	public void register() {
		CombatNumbersEvents.COMBAT.register(this::onCombat);
	}

	private void onCombat(CombatEvent event) {
		if (!config.get(Configs.ENABLED))
			return;

		ServerLevel level = entities.resolve(event.entityId());
		if (level == null)
			return;

		if (!filterRegistry.passes(event, level))
			return;

		Style style = ruleEngine.resolve(event, level);
		CombatNumbersEvents.DISPATCH.invoker().onEvent(
				new DispatchEvent(event.entityId(), event.value(), style.skinId(), style.animationId()));
	}
}
