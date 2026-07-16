package cromveil.combatnumbers.fabric;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;

import net.fabricmc.api.ModInitializer;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.skins.SkinRegistry;

import cromveil.combatnumbers.fabric.impl.config.ConfigFiles;
import cromveil.combatnumbers.fabric.impl.platform.FabricNetwork;
import cromveil.combatnumbers.fabric.impl.platform.FabricServerLifecycle;
import cromveil.combatnumbers.fabric.impl.resource.FabricReloadRegistry;

import cromveil.combatnumbers.modules.server.BroadcastModule;
import cromveil.combatnumbers.modules.server.EntityLevelResolver;
import cromveil.combatnumbers.modules.server.ReadDatapacksModule;
import cromveil.combatnumbers.modules.server.StylingModule;
import cromveil.combatnumbers.modules.server.SyncToClientModule;

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

		var entityResolver = new EntityLevelResolver(lifecycle);
		var datapacks = new ReadDatapacksModule(reloadRegistry, animationRegistry, skinRegistry, ruleEngine, filterRegistry);
		var syncToClient = new SyncToClientModule(network, lifecycle, entityResolver, animationRegistry, skinRegistry, styleTable);
		var styling = new StylingModule(config, ruleEngine, filterRegistry, entityResolver);
		var broadcast = new BroadcastModule(config, network, styleTable, entityResolver);

		IServerSetup.registerAll(
				entityResolver,
				datapacks,
				styling,
				syncToClient, broadcast
		);
	}
}
