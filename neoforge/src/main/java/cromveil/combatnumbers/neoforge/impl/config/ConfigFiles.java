package cromveil.combatnumbers.neoforge.impl.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import cromveil.combatnumbers.config.CombatNumbersOptions;
import cromveil.combatnumbers.core.config.ConfigDef;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.config.IConfigWriter;

public final class ConfigFiles {

	private static final Map<ModConfig.Type, NeoForgeConfig> cache = new ConcurrentHashMap<>();
	private static final Set<ModConfig.Type> registeredTypes = ConcurrentHashMap.newKeySet();

	private ConfigFiles() {}

	public static NeoForgeConfig of(IEventBus eventBus, ModContainer container,
			ModConfig.Type type, List<ConfigDef<?>> defs) {
		var config = cache.computeIfAbsent(type,
				k -> new NeoForgeConfig(defs));
		if (registeredTypes.add(type)) {
			config.init(eventBus);
			container.registerConfig(type, config.spec());
		}
		return config;
	}

	public static void registerConfigScreen(ModContainer container,
			IConfigState state, IConfigWriter writer) {
		container.registerExtensionPoint(IConfigScreenFactory.class,
				(c, screen) -> CombatNumbersOptions.createScreen(screen, state, writer));
	}
}
