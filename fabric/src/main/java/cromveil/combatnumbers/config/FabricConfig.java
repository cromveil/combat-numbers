package cromveil.combatnumbers.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cromveil.combatnumbers.Constants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FabricConfig implements ConfigStore {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final Path COMMON_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("combatnumbers-common.json");
	private static final Path CLIENT_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("combatnumbers-client.json");
	// private static final Path SERVER_PATH = FabricLoader.getInstance()
	// 		.getConfigDir().resolve("combatnumbers-server.json");

	private final Map<String, Object> commonValues = new LinkedHashMap<>();
	private final Map<String, Object> clientValues = new LinkedHashMap<>();
	// private final Map<String, Object> serverValues = new LinkedHashMap<>();
	private final List<Runnable> changeListeners = new ArrayList<>();

	public FabricConfig() {
		load(COMMON_PATH, commonValues, ConfigIds.ALL_COMMON);
		load(CLIENT_PATH, clientValues, ConfigIds.ALL_CLIENT);
		// load(SERVER_PATH, serverValues, ConfigIds.ALL_SERVER);
	}

	@SuppressWarnings("unchecked")
	private static void load(Path path, Map<String, Object> target, List<ConfigId<?>> ids) {
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				Map<String, Object> fromFile = GSON.fromJson(reader, Map.class);
				if (fromFile != null) {
					target.putAll(fromFile);
				}
			} catch (Exception e) {
				Constants.LOG.warn("Failed to read config {}, using defaults", path, e);
			}
		}

		for (ConfigId<?> id : ids) {
			target.putIfAbsent(id.key(), id.defaultValue());
		}

		if (!Files.exists(path)) {
			save(path, target);
		}
	}

	private static void save(Path path, Map<String, Object> values) {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(values, writer);
			}
		} catch (IOException e) {
			Constants.LOG.warn("Failed to write config {}", path, e);
		}
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> T get(ConfigId<T> id) {
		Map<String, Object> store = switch (id.category()) {
			case COMMON -> commonValues;
			case CLIENT -> clientValues;
			// case SERVER -> serverValues;
			default -> null;
		};
		if (store == null) {
			return id.defaultValue();
		}
		Object raw = store.get(id.key());
		if (raw == null) {
			return id.defaultValue();
		}
		if (id.kind() == ConfigId.Kind.ENUM_CYCLE && raw instanceof String s) {
			T def = id.defaultValue();
			Class enumClass = ((Enum) def).getDeclaringClass();
			try {
				return (T) Enum.valueOf(enumClass, s);
			} catch (IllegalArgumentException e) {
				return def;
			}
		}
		return (T) raw;
	}

	@Override
	public <T> void set(ConfigId<T> id, T value) {
		Map<String, Object> store = switch (id.category()) {
			case COMMON -> commonValues;
			case CLIENT -> clientValues;
			// case SERVER -> serverValues;
			default -> null;
		};
		if (store != null) {
			store.put(id.key(), value);
		}
	}

	@Override
	public void save() {
		save(COMMON_PATH, commonValues);
		save(CLIENT_PATH, clientValues);
		// save(SERVER_PATH, serverValues);
		for (Runnable listener : changeListeners) {
			listener.run();
		}
	}

	@Override
	public void addChangeListener(Runnable listener) {
		changeListeners.add(listener);
	}
}
