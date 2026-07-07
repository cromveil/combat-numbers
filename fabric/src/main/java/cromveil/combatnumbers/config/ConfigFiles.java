package cromveil.combatnumbers.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cromveil.combatnumbers.core.config.ConfigBundle;
import cromveil.combatnumbers.core.config.ConfigDef;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigFiles {

	private static final Map<String, FabricConfig> cache = new ConcurrentHashMap<>();

	private ConfigFiles() {}

	public static ConfigBundle load(String path, List<ConfigDef<?>> defs) {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		var config = cache.computeIfAbsent(path,
				k -> new FabricConfig(configDir.resolve(k), defs));
		return new ConfigBundle(config, config);
	}

	static void reloadAll() {
		for (FabricConfig config : cache.values()) {
			config.reload();
		}
	}
}
