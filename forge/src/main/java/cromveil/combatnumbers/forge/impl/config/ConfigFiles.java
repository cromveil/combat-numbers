package cromveil.combatnumbers.forge.impl.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;

import cromveil.combatnumbers.config.CombatNumbersOptions;
import cromveil.combatnumbers.core.config.ConfigDef;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.config.IConfigWriter;

public final class ConfigFiles {

	private static final Map<ModConfig.Type, ForgeConfig> cache = new ConcurrentHashMap<>();
	private static final Set<ModConfig.Type> registeredTypes = ConcurrentHashMap.newKeySet();

	private ConfigFiles() {}

	public static ForgeConfig of(IEventBus modEventBus, ModContainer container,
			ModConfig.Type type, List<ConfigDef<?>> defs) {
		var config = cache.computeIfAbsent(type,
				k -> new ForgeConfig(defs));
		if (registeredTypes.add(type)) {
			config.init(modEventBus);
			container.addConfig(new ModConfig(type, config.spec(), container));
		}
		return config;
	}

	public static void registerConfigScreen(ModContainer container,
			IConfigState state, IConfigWriter writer) {
		container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory(
						(mc, screen) -> CombatNumbersOptions.createScreen(screen, state, writer)));
	}
}
