package cromveil.combatnumbers.neoforge;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.minecraft.server.level.ServerLevel;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.skins.SkinRegistry;

import cromveil.combatnumbers.neoforge.impl.config.ConfigFiles;
import cromveil.combatnumbers.neoforge.impl.platform.NeoForgeNetwork;
import cromveil.combatnumbers.neoforge.impl.platform.NeoForgeServerLifecycle;
import cromveil.combatnumbers.neoforge.impl.resource.NeoForgeReloadRegistry;

import cromveil.combatnumbers.modules.server.BroadcastModule;
import cromveil.combatnumbers.modules.server.EntityLevelResolver;
import cromveil.combatnumbers.modules.server.ReadDatapacksModule;
import cromveil.combatnumbers.modules.server.StylingModule;
import cromveil.combatnumbers.modules.server.SyncToClientModule;

@Mod(Constants.MOD_ID)
public class CombatNumbers {

	public CombatNumbers(IEventBus modEventBus, ModContainer container) {
		var network = new NeoForgeNetwork(modEventBus);
		var lifecycle = new NeoForgeServerLifecycle();
		var reloadRegistry = new NeoForgeReloadRegistry(modEventBus);

		var config = ConfigFiles.of(modEventBus, container, ModConfig.Type.COMMON, Configs.COMMON);
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
