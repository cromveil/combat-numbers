package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.theme.ThemeManager;
import cromveil.combatnumbers.config.screen.ConfigOption;
import cromveil.combatnumbers.config.screen.SliderFormat;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ConfigId<T> {

	public enum Category { CLIENT, SERVER }

	public enum Kind { BOOL, DOUBLE_SLIDER, STRING_CYCLE, ENUM_CYCLE }

	private final Category category;
	private final String key;
	private final Kind kind;
	private final T defaultValue;
	private final double min, max;
	private final SliderFormat sliderFormat;
	private final Supplier<List<String>> allowedValuesSupplier;
	private final boolean allowEmpty;
	private final Function<T, Component> displayFn;

	private ConfigId(Category category, String key, Kind kind, T defaultValue,
			double min, double max, SliderFormat sliderFormat,
			Supplier<List<String>> allowedValuesSupplier, boolean allowEmpty,
			Function<T, Component> displayFn) {
		this.category = category;
		this.key = key;
		this.kind = kind;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.sliderFormat = sliderFormat;
		this.allowedValuesSupplier = allowedValuesSupplier;
		this.allowEmpty = allowEmpty;
		this.displayFn = displayFn;
	}

	public static ConfigId<Boolean> bool(Category category, String key, boolean defaultValue) {
		return new ConfigId<>(category, key, Kind.BOOL, defaultValue,
				0, 0, null, null, false, null);
	}

	public static ConfigId<Double> floatSlider(Category category, String key, double defaultValue,
			double min, double max, SliderFormat format) {
		return new ConfigId<>(category, key, Kind.DOUBLE_SLIDER, defaultValue,
				min, max, format, null, false, null);
	}

	public static ConfigId<String> stringCycle(Category category, String key, String defaultValue,
			Supplier<List<String>> values, boolean allowEmpty) {
		return new ConfigId<>(category, key, Kind.STRING_CYCLE, defaultValue,
				0, 0, null, values, allowEmpty, null);
	}

	public static ConfigId<String> stringCycle(Category category, String key, String defaultValue,
			Supplier<List<String>> values, Function<String, Component> displayFn, boolean allowEmpty) {
		return new ConfigId<>(category, key, Kind.STRING_CYCLE, defaultValue,
				0, 0, null, values, allowEmpty, displayFn);
	}

	public static <E extends Enum<E>> ConfigId<E> enumCycle(Category category, String key,
			E defaultValue, Function<E, Component> displayFn) {
		@SuppressWarnings("unchecked")
		Function<E, Component> fn = (Function<E, Component>) displayFn;
		return new ConfigId<>(category, key, Kind.ENUM_CYCLE, defaultValue,
				0, 0, null, null, false, fn);
	}

	public Category category() { return category; }
	public String key() { return key; }
	public Kind kind() { return kind; }
	public T defaultValue() { return defaultValue; }
	public double min() { return min; }
	public double max() { return max; }
	public SliderFormat sliderFormat() { return sliderFormat; }
	public Supplier<List<String>> allowedValuesSupplier() { return allowedValuesSupplier; }
	public boolean allowEmpty() { return allowEmpty; }
	public Function<T, Component> displayFn() { return displayFn; }

	@SuppressWarnings("unchecked")
	public ConfigOption<?> toOption(ConfigStore store) {
		return switch (kind) {
			case BOOL -> {
				ConfigId<Boolean> self = (ConfigId<Boolean>) (Object) this;
				yield ConfigOption.ofBool(key, self.defaultValue,
						() -> store.get(self), v -> store.set(self, v));
			}
			case DOUBLE_SLIDER -> {
				ConfigId<Double> self = (ConfigId<Double>) (Object) this;
				yield ConfigOption.ofSlider(key, self.defaultValue, min, max,
						() -> store.get(self), v -> store.set(self, v),
						sliderFormat);
			}
			case STRING_CYCLE -> {
				ConfigId<String> self = (ConfigId<String>) (Object) this;
				List<String> values = allowedValuesSupplier.get();
				@SuppressWarnings("unchecked")
				Function<String, Component> disp = (Function<String, Component>) (Object) displayFn;
				if (disp == null) {
					disp = Component::literal;
				}
				Function<Object, Component> descFn = null;
				if (displayFn != null) {
					descFn = current -> {
						String currentId = (String) current;
						if (currentId == null || currentId.isEmpty()) {
							return Component.translatable("config.combatnumbers.option.theme.off.description");
						}
						String desc = ThemeManager.description(currentId);
						return desc != null ? Component.literal(desc) : null;
					};
				}
				yield ConfigOption.ofStringCycle(key, self.defaultValue,
						() -> store.get(self), v -> store.set(self, v),
						values, disp, descFn, allowEmpty,
						Component.translatable("options.off"));
			}
			case ENUM_CYCLE -> {
				@SuppressWarnings({"unchecked", "rawtypes"})
				ConfigOption opt = ConfigOption.ofEnum(key, (Enum) defaultValue,
						() -> (Enum) store.get((ConfigId) this),
						v -> store.set((ConfigId) this, v),
						(Function) displayFn);
				yield opt;
			}
		};
	}
}
