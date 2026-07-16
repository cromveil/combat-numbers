package cromveil.combatnumbers.forge;

import java.util.Map;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.client.theme.ThemeLoader;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.config.MergedConfig;

import cromveil.combatnumbers.forge.impl.config.ConfigFiles;
import cromveil.combatnumbers.forge.impl.platform.ForgeNetwork;
import cromveil.combatnumbers.forge.impl.resource.ForgeReloadRegistry;

import cromveil.combatnumbers.modules.client.ThemeModule;
import cromveil.combatnumbers.modules.client.ReadResourcePacksModule;
import cromveil.combatnumbers.forge.modules.client.FloatingTextRendererModule;
import cromveil.combatnumbers.forge.modules.client.SyncReceiver;

public final class CombatNumbersClient {

	private CombatNumbersClient() {}

	public static void setup(IEventBus modEventBus, ModContainer container,
			ForgeNetwork network, IConfigState commonConfig) {

		var textManager = new FloatingTextManager();
		var skinResolver = new SkinResolver();
		var animationResolver = new AnimationResolver();

		var serverStyles = new SyncReceiver(animationResolver, skinResolver);
		var factory = new FloatingTextFactory(
				commonConfig, textManager, skinResolver,
				animationResolver, new AnimationCompiler(), serverStyles::styleTable);

		ForgeClientBridge.init(factory, serverStyles);

		var clientConfig = ConfigFiles.of(modEventBus, container, ModConfig.Type.CLIENT, Configs.CLIENT);
		var config = new MergedConfig(Map.of(
				Category.COMMON, commonConfig,
				Category.CLIENT, clientConfig));
		ConfigFiles.registerConfigScreen(container, config, config);

		var resourcePacks = new ReadResourcePacksModule(skinResolver, animationResolver,
				new ForgeReloadRegistry(modEventBus));
		var theme = new ThemeModule(config, new ThemeLoader(), skinResolver, animationResolver,
				resourcePacks::resources);
		resourcePacks.setOnComplete(theme::reload);

		var renderer = new FloatingTextRendererModule(config, textManager, skinResolver, animationResolver,
				new AnimationCompiler(), serverStyles::styleTable);

		MixinBridge.init(new RenderContext(config, textManager));

		IClientSetup.registerAll(
			resourcePacks, theme, serverStyles,
			renderer
		);
	}
}
