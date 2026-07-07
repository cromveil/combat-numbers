package cromveil.combatnumbers.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cromveil.combatnumbers.core.config.ConfigDef;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigFiles {

	private static final Map<String, FabricConfig> cache = new ConcurrentHashMap<>();
	private static boolean reloadRegistered;

	private ConfigFiles() {}

	public static FabricConfig load(String path, List<ConfigDef<?>> defs) {
		if (!reloadRegistered) {
			reloadRegistered = true;
			ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
					(server, resourceManager, success) -> reloadAll());
		}
		Path configDir = FabricLoader.getInstance().getConfigDir();
		return cache.computeIfAbsent(path,
				k -> new FabricConfig(configDir.resolve(k), defs));
	}

	static void reloadAll() {
		for (FabricConfig config : cache.values()) {
			config.reload();
		}
	}
}
