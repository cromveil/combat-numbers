package cromveil.combatnumbers.neoforge;

import java.util.Map;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.client.theme.ThemeLoader;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.core.config.MergedConfig;

import cromveil.combatnumbers.neoforge.impl.config.ConfigFiles;
import cromveil.combatnumbers.neoforge.impl.resource.NeoForgeReloadRegistry;

import cromveil.combatnumbers.modules.client.ReadResourcePacksModule;
import cromveil.combatnumbers.modules.client.ThemeModule;
import cromveil.combatnumbers.neoforge.modules.client.FloatingTextRendererModule;
import cromveil.combatnumbers.neoforge.modules.client.SyncReceiver;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class CombatNumbersClient {

	public CombatNumbersClient(IEventBus modEventBus, ModContainer container) {
		var textManager = new FloatingTextManager();
		var skinResolver = new SkinResolver();
		var animationResolver = new AnimationResolver();

		var commonConfig = ConfigFiles.of(modEventBus, container, ModConfig.Type.COMMON, Configs.COMMON);
		var clientConfig = ConfigFiles.of(modEventBus, container, ModConfig.Type.CLIENT, Configs.CLIENT);
		var config = new MergedConfig(Map.of(
				Category.COMMON, commonConfig,
				Category.CLIENT, clientConfig));
		ConfigFiles.registerConfigScreen(container, config, config);

		var resourcePacks = new ReadResourcePacksModule(skinResolver, animationResolver,
				new NeoForgeReloadRegistry(modEventBus));
		var theme = new ThemeModule(config, new ThemeLoader(), skinResolver, animationResolver,
				resourcePacks::resources);
		resourcePacks.setOnComplete(theme::reload);
		var serverStyles = new SyncReceiver(animationResolver, skinResolver);
		var renderer = new FloatingTextRendererModule(config, textManager, skinResolver, animationResolver, new AnimationCompiler(), serverStyles::styleTable);

		ClientPayloadHandlers.init(animationResolver, skinResolver, renderer.factory());

		MixinBridge.init(new RenderContext(config, textManager));

		IClientSetup.registerAll(
			resourcePacks, theme, serverStyles,
			renderer
		);
	}
}
