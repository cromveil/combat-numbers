package cromveil.combatnumbers.fabric;

import java.util.Map;

import net.fabricmc.api.ClientModInitializer;

import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.client.theme.ThemeLoader;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.core.config.MergedConfig;

import cromveil.combatnumbers.fabric.impl.config.ConfigFiles;
import cromveil.combatnumbers.fabric.impl.resource.FabricReloadRegistry;

import cromveil.combatnumbers.modules.client.ReadResourcePacksModule;
import cromveil.combatnumbers.modules.client.ThemeModule;
import cromveil.combatnumbers.fabric.modules.client.FloatingTextRendererModule;
import cromveil.combatnumbers.fabric.modules.client.SyncReceiver;

public class CombatNumbersClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		var textManager = new FloatingTextManager();
		var skinResolver = new SkinResolver();
		var animationResolver = new AnimationResolver();

		var commonConfig = ConfigFiles.load("combatnumbers-common.json", Configs.COMMON);
		var clientConfig = ConfigFiles.load("combatnumbers-client.json", Configs.CLIENT);
		var config = new MergedConfig(Map.of(
				Category.COMMON, commonConfig,
				Category.CLIENT, clientConfig));

		var resourcePacks = new ReadResourcePacksModule(skinResolver, animationResolver,
				new FabricReloadRegistry());
		var theme = new ThemeModule(config, new ThemeLoader(), skinResolver, animationResolver,
				resourcePacks::resources);
		resourcePacks.setOnComplete(theme::reload);

		var serverStyles = new SyncReceiver(animationResolver, skinResolver);
		var renderer = new FloatingTextRendererModule(config, textManager, skinResolver, animationResolver, new AnimationCompiler(), serverStyles::styleTable);

		MixinBridge.init(new RenderContext(config, textManager));

		IClientSetup.registerAll(
			resourcePacks, theme, serverStyles, 
			renderer
		);
	}
}
