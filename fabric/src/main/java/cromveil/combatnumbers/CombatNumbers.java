package cromveil.combatnumbers;

import java.util.function.Supplier;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.ConfigFiles;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.config.FabricConfigReloadModule;
import cromveil.combatnumbers.core.Setup;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.platform.FabricNetwork;
import cromveil.combatnumbers.platform.FabricServerLifecycle;
import cromveil.combatnumbers.resource.FabricReloadRegistry;
import cromveil.combatnumbers.server.BroadcastModule;
import cromveil.combatnumbers.server.EntityLevelResolver;
import cromveil.combatnumbers.server.ReadDatapacksModule;
import cromveil.combatnumbers.server.StylingModule;
import cromveil.combatnumbers.server.SyncToClientModule;
import cromveil.combatnumbers.skins.SkinRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerLevel;

public class CombatNumbers implements ModInitializer {

	@Override
	public void onInitialize() {
		var network = new FabricNetwork();
		var lifecycle = new FabricServerLifecycle();
		var reloadRegistry = new FabricReloadRegistry();

		var config = ConfigFiles.load("combatnumbers-common.json", Configs.COMMON);
		var animationRegistry = new AnimationRegistry();
		var skinRegistry = new SkinRegistry();
		var ruleEngine = new RuleEngine<ServerLevel>();
		var filterRegistry = new FilterRegistry<ServerLevel>();
		Supplier<StyleTable> styleTable = () -> StyleTable.from(ruleEngine);

		var configReloadListener = new FabricConfigReloadModule();
		var entityResolver = new EntityLevelResolver(lifecycle);
		var datapacks = new ReadDatapacksModule(reloadRegistry, animationRegistry, skinRegistry, ruleEngine, filterRegistry);
		var syncToClient = new SyncToClientModule(network, lifecycle, entityResolver, animationRegistry, skinRegistry, styleTable);
		var styling = new StylingModule(config.state(), ruleEngine, filterRegistry, entityResolver);
		var broadcast = new BroadcastModule(config.state(), network, styleTable, entityResolver);

		Setup.registerAll(
				entityResolver, configReloadListener,
				datapacks,
				styling,
				syncToClient, broadcast
		);
	}
}
