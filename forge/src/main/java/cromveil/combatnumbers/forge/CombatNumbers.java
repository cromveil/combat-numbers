package cromveil.combatnumbers.forge;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.eventbus.api.bus.BusGroup;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.skins.SkinRegistry;

import cromveil.combatnumbers.forge.impl.config.ConfigFiles;
import cromveil.combatnumbers.forge.impl.platform.ForgeNetwork;
import cromveil.combatnumbers.forge.impl.platform.ForgeServerLifecycle;
import cromveil.combatnumbers.forge.impl.resource.ForgeReloadRegistry;

import cromveil.combatnumbers.modules.server.BroadcastModule;
import cromveil.combatnumbers.modules.server.EntityLevelResolver;
import cromveil.combatnumbers.modules.server.ReadDatapacksModule;
import cromveil.combatnumbers.modules.server.StylingModule;
import cromveil.combatnumbers.modules.server.SyncToClientModule;

@Mod(Constants.MOD_ID)
public class CombatNumbers {

	public CombatNumbers(FMLJavaModLoadingContext context) {
		BusGroup modBusGroup = context.getModBusGroup();
		ModContainer container = context.getContainer();

		var network = new ForgeNetwork();
		var lifecycle = new ForgeServerLifecycle();
		var reloadRegistry = new ForgeReloadRegistry();

		var commonConfig = ConfigFiles.of(modBusGroup, container, ModConfig.Type.COMMON, Configs.COMMON);
		var animationRegistry = new AnimationRegistry();
		var skinRegistry = new SkinRegistry();
		var ruleEngine = new RuleEngine<ServerLevel>();
		var filterRegistry = new FilterRegistry<ServerLevel>();
		Supplier<StyleTable> styleTable = () -> StyleTable.from(ruleEngine);

		var entityResolver = new EntityLevelResolver(lifecycle);
		var datapacks = new ReadDatapacksModule(reloadRegistry, animationRegistry, skinRegistry, ruleEngine, filterRegistry);
		var syncToClient = new SyncToClientModule(network, lifecycle, entityResolver, animationRegistry, skinRegistry, styleTable);
		var styling = new StylingModule(commonConfig, ruleEngine, filterRegistry, entityResolver);
		var broadcast = new BroadcastModule(commonConfig, network, styleTable, entityResolver);

		IServerSetup.registerAll(
				entityResolver,
				datapacks,
				styling,
				syncToClient, broadcast
		);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			CombatNumbersClient.setup(modBusGroup, container, network, commonConfig);
		}
	}
}
