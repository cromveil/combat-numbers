package cromveil.combatnumbers;

import java.util.Map;

import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.ReadResourcePacksModule;
import cromveil.combatnumbers.client.ThemeModule;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.client.theme.ThemeLoader;
import cromveil.combatnumbers.config.ConfigFiles;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.ISetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.core.config.MergedConfig;
import cromveil.combatnumbers.resource.FabricReloadRegistry;
import net.fabricmc.api.ClientModInitializer;

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

		ISetup.registerAll(
			resourcePacks, theme, serverStyles, 
			renderer
		);
	}
}
