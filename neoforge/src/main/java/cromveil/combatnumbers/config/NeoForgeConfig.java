package cromveil.combatnumbers.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NeoForgeConfig implements ConfigStore {

	private static final NeoForgeConfig INSTANCE = new NeoForgeConfig();

	public static NeoForgeConfig instance() { return INSTANCE; }

	private final ModConfigSpec commonSpec;
	private final ModConfigSpec clientSpec;
	private final Map<String, ConfigValue<?>> commonValues = new HashMap<>();
	private final Map<String, ConfigValue<?>> clientValues = new HashMap<>();

	private NeoForgeConfig() {
		this.commonSpec = buildSpec(ConfigIds.ALL_COMMON, commonValues);
		this.clientSpec = buildSpec(ConfigIds.ALL_CLIENT, clientValues);
	}

	public ModConfigSpec commonSpec() { return commonSpec; }
	public ModConfigSpec clientSpec() { return clientSpec; }

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
	private static ModConfigSpec buildSpec(List<ConfigId<?>> ids, Map<String, ConfigValue<?>> out) {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		for (ConfigId<?> id : ids) {
			ConfigValue<?> cv = switch (id.kind()) {
				case BOOL ->
						builder.define(camelToSnake(id.key()), (Boolean) id.defaultValue());
				case DOUBLE_SLIDER -> {
					double def = ((Number) id.defaultValue()).doubleValue();
					yield builder.defineInRange(camelToSnake(id.key()), def,
							(double) id.min(), (double) id.max());
				}
				case STRING_CYCLE -> {
					List<String> vals = new ArrayList<>();
					if (id.allowEmpty()) vals.add("");
					for (String v : id.allowedValuesSupplier().get()) {
						if (!vals.contains(v)) vals.add(v);
					}
					if (!vals.contains("default")) vals.add("default");
					yield builder.defineInList(camelToSnake(id.key()),
							(String) id.defaultValue(), vals);
				}
				case ENUM_CYCLE ->
						builder.defineEnum(camelToSnake(id.key()), (Enum) id.defaultValue());
			};
			out.put(id.key(), cv);
		}
		return builder.build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(ConfigId<T> id) {
		ConfigValue<?> cv = switch (id.category()) {
			case COMMON -> commonValues.get(id.key());
			case CLIENT -> clientValues.get(id.key());
			case SERVER -> null;
		};
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
	public <T> void set(ConfigId<T> id, T value) {
		ConfigValue<?> cv = switch (id.category()) {
			case COMMON -> commonValues.get(id.key());
			case CLIENT -> clientValues.get(id.key());
			case SERVER -> null;
		};
		if (cv != null) {
			try {
				((ConfigValue<T>) cv).set(value);
			} catch (Exception ignored) {}
		}
	}

	@Override
	public void save() {
		saveSpec(commonValues);
		saveSpec(clientValues);
	}

	private static void saveSpec(Map<String, ConfigValue<?>> values) {
		var iter = values.values().iterator();
		if (iter.hasNext()) {
			try {
				iter.next().save();
			} catch (Exception ignored) {
			}
		}
	}

	@Override
	public void addChangeListener(Runnable listener) {}
}
