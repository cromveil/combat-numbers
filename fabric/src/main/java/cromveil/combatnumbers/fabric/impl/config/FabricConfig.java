package cromveil.combatnumbers.fabric.impl.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.config.ConfigDef;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.config.IConfigWriter;

public final class FabricConfig implements IConfigState, IConfigWriter {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path path;
	private final List<ConfigDef<?>> defs;
	private final Map<String, Object> values = new LinkedHashMap<>();
	private final List<Runnable> changeListeners = new ArrayList<>();
	private final Map<String, List<Runnable>> keyListeners = new LinkedHashMap<>();
	private Map<String, Object> previousValues;

	public FabricConfig(Path path, List<ConfigDef<?>> defs) {
		this.path = path;
		this.defs = defs;
		load();
		previousValues = new LinkedHashMap<>(values);
	}

	@SuppressWarnings("unchecked")
	private void load() {
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				Map<String, Object> fromFile = GSON.fromJson(reader, Map.class);
				if (fromFile != null) {
					values.putAll(fromFile);
				}
			} catch (Exception e) {
				Constants.LOG.warn("Failed to read config {}, using defaults", path, e);
			}
		}

		for (ConfigDef<?> id : defs) {
			values.putIfAbsent(id.key(), id.defaultValue());
		}

		if (!Files.exists(path)) {
			saveToFile();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T get(ConfigDef<T> id) {
		Object raw = values.get(id.key());
		if (raw == null) {
			return id.defaultValue();
		}
		if (id.valueType() == ConfigDef.ValueType.ENUM && raw instanceof String s) {
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
	public <T> void setValue(ConfigDef<T> id, T value) {
		values.put(id.key(), value);
	}

	@Override
	public void commit() {
		saveToFile();
		for (Runnable listener : changeListeners) {
			listener.run();
		}
		fireKeyListeners();
	}

	@Override
	public void onChanged(Runnable listener) {
		changeListeners.add(listener);
	}

	@Override
	public <T> void onChanged(ConfigDef<T> key, Runnable listener) {
		keyListeners.computeIfAbsent(key.key(), k -> new ArrayList<>()).add(listener);
	}

	public void reload() {
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> fromFile = GSON.fromJson(reader, Map.class);
				if (fromFile != null) {
					values.clear();
					values.putAll(fromFile);
				}
			} catch (Exception e) {
				Constants.LOG.warn("Failed to read config {}, using defaults", path, e);
			}
		}

		for (ConfigDef<?> id : defs) {
			values.putIfAbsent(id.key(), id.defaultValue());
		}

		for (Runnable listener : changeListeners) {
			listener.run();
		}
		fireKeyListeners();
	}

	private void fireKeyListeners() {
		for (var entry : keyListeners.entrySet()) {
			String key = entry.getKey();
			if (!Objects.equals(previousValues.get(key), values.get(key))) {
				previousValues.put(key, values.get(key));
				for (Runnable listener : entry.getValue()) {
					listener.run();
				}
			}
		}
	}

	private void saveToFile() {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(values, writer);
			}
		} catch (IOException e) {
			Constants.LOG.warn("Failed to write config {}", path, e);
		}
	}
}
