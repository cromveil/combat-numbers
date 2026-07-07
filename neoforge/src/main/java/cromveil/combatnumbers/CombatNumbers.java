package cromveil.combatnumbers;

import java.util.function.Supplier;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.ConfigFiles;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.ISetup;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.platform.NeoForgeNetwork;
import cromveil.combatnumbers.platform.NeoForgeServerLifecycle;
import cromveil.combatnumbers.resource.NeoForgeReloadRegistry;
import cromveil.combatnumbers.server.BroadcastModule;
import cromveil.combatnumbers.server.EntityLevelResolver;
import cromveil.combatnumbers.server.ReadDatapacksModule;
import cromveil.combatnumbers.server.StylingModule;
import cromveil.combatnumbers.server.SyncToClientModule;
import cromveil.combatnumbers.skins.SkinRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.minecraft.server.level.ServerLevel;

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

		ISetup.registerAll(
				entityResolver,
				datapacks,
				styling,
				syncToClient, broadcast
		);
	}
}
