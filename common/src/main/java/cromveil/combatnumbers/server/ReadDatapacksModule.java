package cromveil.combatnumbers.server;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.core.events.CombatNumbersEvents;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.filters.FilterLoader;
import cromveil.combatnumbers.resource.IReloadListenerRegistry;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.skins.SkinRegistry;
import cromveil.combatnumbers.styles.RuleLoader;
import cromveil.combatnumbers.styles.RuleSet;
import cromveil.combatnumbers.styles.WhenCondition;
import net.minecraft.server.level.ServerLevel;

public final class ReadDatapacksModule implements IServerSetup {

	private final IReloadListenerRegistry reloadRegistry;
	private final AnimationRegistry animationRegistry;
	private final SkinRegistry skinRegistry;
	private final RuleEngine<ServerLevel> ruleEngine;
	private final FilterRegistry<ServerLevel> filterRegistry;

	private int loadedCount;

	public ReadDatapacksModule(IReloadListenerRegistry reloadRegistry,
			AnimationRegistry animationRegistry, SkinRegistry skinRegistry,
			RuleEngine<ServerLevel> ruleEngine, FilterRegistry<ServerLevel> filterRegistry) {
		this.reloadRegistry = reloadRegistry;
		this.animationRegistry = animationRegistry;
		this.skinRegistry = skinRegistry;
		this.ruleEngine = ruleEngine;
		this.filterRegistry = filterRegistry;
	}

	@Override
	public void register() {
		reloadRegistry.registerServerData(
				StableId.of(Constants.MOD_ID, "animations"),
				"animations", TimelineCodec.CODEC,
				(data, resources) -> {
					animationRegistry.accept(data);
					afterReload();
				});

		reloadRegistry.registerServerData(
				StableId.of(Constants.MOD_ID, "skins"),
				"skins", SkinDefinition.CODEC,
				(data, resources) -> {
					skinRegistry.accept(data, resources);
					afterReload();
				});

		var ruleLoader = new RuleLoader(ruleEngine);
		reloadRegistry.registerServerData(
				StableId.of(Constants.MOD_ID, "styles"),
				"styles", RuleSet.CODEC,
				(data, resources) -> {
					ruleLoader.accept(data);
					afterReload();
				});

		var filterLoader = new FilterLoader(filterRegistry);
		reloadRegistry.registerServerData(
				StableId.of(Constants.MOD_ID, "filters"),
				"filters", WhenCondition.CODEC.listOf(),
				(data, resources) -> {
					filterLoader.accept(data);
					afterReload();
				});
	}

	private void afterReload() {
		// prevents duplicate fires. Little bit hacky though, since it relies on all reload listeners to fire.
		loadedCount++;
		if (loadedCount >= 4) {
			loadedCount = 0;
			CombatNumbersEvents.DATA_RELOADED.invoker().run();
		}
	}
}
