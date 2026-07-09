package cromveil.combatnumbers.forge.impl.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import cromveil.combatnumbers.core.config.ConfigDef;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.config.IConfigWriter;

public final class ForgeConfig implements IConfigState, IConfigWriter {

	private final ForgeConfigSpec spec;
	private final Map<String, ConfigValue<?>> values = new HashMap<>();
	private final List<Runnable> changeListeners = new ArrayList<>();
	private final List<DeferredKeyListener> pendingKeyListeners = new ArrayList<>();
	private Map<String, Object> previousValues;
	private BusGroup modBusGroup;

	public ForgeConfig(List<ConfigDef<?>> defs) {
		this.spec = buildSpec(defs, values);
	}

	public ForgeConfigSpec spec() {
		return spec;
	}

	private static String camelToSnake(String camel) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < camel.length(); i++) {
			char c = camel.charAt(i);
			if (Character.isUpperCase(c)) {
				if (i > 0) sb.append('_');
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static ForgeConfigSpec buildSpec(List<ConfigDef<?>> ids, Map<String, ConfigValue<?>> out) {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		for (ConfigDef<?> id : ids) {
			ConfigValue<?> cv = switch (id.valueType()) {
				case BOOL ->
						builder.define(camelToSnake(id.key()), (Boolean) id.defaultValue());
				case NUMBER -> {
					double def = ((Number) id.defaultValue()).doubleValue();
					yield builder.defineInRange(camelToSnake(id.key()), def,
							(double) id.min(), (double) id.max());
				}
				case STRING_LIST -> {
					List<String> vals = new ArrayList<>();
					if (id.allowEmpty()) vals.add("");
					for (String v : id.allowedValuesSupplier().get()) {
						if (!vals.contains(v)) vals.add(v);
					}
					if (!vals.contains("default")) vals.add("default");
					yield builder.defineInList(camelToSnake(id.key()),
							(String) id.defaultValue(), vals);
				}
				case ENUM ->
						builder.defineEnum(camelToSnake(id.key()), (Enum) id.defaultValue());
			};
			out.put(id.key(), cv);
		}
		return builder.build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(ConfigDef<T> id) {
		ConfigValue<?> cv = values.get(id.key());
		if (cv == null) {
			return id.defaultValue();
		}
		try {
			return (T) cv.get();
		} catch (IllegalStateException e) {
			return id.defaultValue();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void setValue(ConfigDef<T> id, T value) {
		ConfigValue<?> cv = values.get(id.key());
		if (cv != null) {
			try {
				((ConfigValue<T>) cv).set(value);
			} catch (Exception ignored) {
			}
		}
	}

	@Override
	public void commit() {
		var iter = values.values().iterator();
		if (iter.hasNext()) {
			try {
				iter.next().save();
			} catch (Exception ignored) {
			}
		}
	}

	public void init(BusGroup modBusGroup) {
		this.modBusGroup = modBusGroup;
		previousValues = snapshotValues();
		for (Runnable listener : changeListeners) {
			registerReloadListener(listener);
		}
		changeListeners.clear();
		for (DeferredKeyListener dkl : pendingKeyListeners) {
			registerKeyReloadListener(dkl.key, dkl.listener);
		}
		pendingKeyListeners.clear();
	}

	@Override
	public void onChanged(Runnable listener) {
		if (modBusGroup != null) {
			registerReloadListener(listener);
		} else {
			changeListeners.add(listener);
		}
	}

	@Override
	public <T> void onChanged(ConfigDef<T> key, Runnable listener) {
		if (modBusGroup != null) {
			registerKeyReloadListener(key.key(), listener);
		} else {
			pendingKeyListeners.add(new DeferredKeyListener(key.key(), listener));
		}
	}

	private void registerReloadListener(Runnable listener) {
		var bus = ModConfigEvent.Reloading.getBus(modBusGroup);
		bus.addListener(event -> {
			if (event.getConfig().getSpec() == spec) {
				listener.run();
			}
		});
	}

	private void registerKeyReloadListener(String key, Runnable listener) {
		var bus = ModConfigEvent.Reloading.getBus(modBusGroup);
		bus.addListener(event -> {
			if (event.getConfig().getSpec() == spec) {
				ConfigValue<?> cv = values.get(key);
				Object newVal = cv != null ? cv.get() : null;
				Object oldVal = previousValues != null ? previousValues.get(key) : null;
				if (!Objects.equals(oldVal, newVal)) {
					listener.run();
					if (previousValues != null) {
						previousValues.put(key, newVal);
					}
				}
			}
		});
	}

	private Map<String, Object> snapshotValues() {
		Map<String, Object> snap = new LinkedHashMap<>();
		for (var entry : values.entrySet()) {
			try {
				snap.put(entry.getKey(), entry.getValue().get());
			} catch (IllegalStateException ignored) {
			}
		}
		return snap;
	}

	private record DeferredKeyListener(String key, Runnable listener) {}
}
