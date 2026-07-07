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
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.Setup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.core.config.MergedConfigState;
import cromveil.combatnumbers.core.config.MergedConfigWriter;
import cromveil.combatnumbers.resource.NeoForgeReloadRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class CombatNumbersClient {

	public CombatNumbersClient(IEventBus modEventBus, ModContainer container) {
		var textManager = new FloatingTextManager();
		var skinResolver = new SkinResolver();
		var animationResolver = new AnimationResolver();

		var commonConfig = ConfigFiles.of(modEventBus, container, ModConfig.Type.COMMON, Configs.COMMON);
		var clientConfig = ConfigFiles.of(modEventBus, container, ModConfig.Type.CLIENT, Configs.CLIENT);
		var configState = new MergedConfigState(Map.of(
				Category.COMMON, commonConfig.state(),
				Category.CLIENT, clientConfig.state()));
		var configWriter = new MergedConfigWriter(Map.of(
				Category.COMMON, commonConfig.writer(),
				Category.CLIENT, clientConfig.writer()));
		ConfigFiles.registerConfigScreen(container, configState, configWriter);
		
		Runnable[] onClientResourcesLoaded = new Runnable[1];
		var resourcePacks = new ReadResourcePacksModule(skinResolver, animationResolver,
				new NeoForgeReloadRegistry(modEventBus), () -> {
					if (onClientResourcesLoaded[0] != null) {
						onClientResourcesLoaded[0].run();
					}
				});
		var theme = new ThemeModule(configState, new ThemeLoader(), skinResolver, animationResolver,
				resourcePacks::resources);
		onClientResourcesLoaded[0] = theme::reload;
		var serverStyles = new SyncReceiver(modEventBus, animationResolver, skinResolver);
		var renderer = new FloatingTextRendererModule(modEventBus, configState, textManager, skinResolver, animationResolver, new AnimationCompiler(), serverStyles::styleTable);

		MixinBridge.init(new RenderContext(configState, textManager));

		Setup.registerAll(
			resourcePacks, theme, serverStyles,
			renderer
		);
	}
}
