package cromveil.combatnumbers.core.config;

import java.util.List;
import java.util.function.Supplier;

public final class ConfigId<T> {

	public enum Category { CLIENT, SERVER, COMMON }

	public enum Kind { BOOL, DOUBLE_SLIDER, STRING_CYCLE, ENUM_CYCLE }

	private final Category category;
	private final String key;
	private final Kind kind;
	private final T defaultValue;
	private final double min, max;
	private final SliderFormat sliderFormat;
	private final Supplier<List<String>> allowedValuesSupplier;
	private final boolean allowEmpty;

	private ConfigId(Category category, String key, Kind kind, T defaultValue,
			double min, double max, SliderFormat sliderFormat,
			Supplier<List<String>> allowedValuesSupplier, boolean allowEmpty) {
		this.category = category;
		this.key = key;
		this.kind = kind;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.sliderFormat = sliderFormat;
		this.allowedValuesSupplier = allowedValuesSupplier;
		this.allowEmpty = allowEmpty;
	}

	public static ConfigId<Boolean> bool(Category category, String key, boolean defaultValue) {
		return new ConfigId<>(category, key, Kind.BOOL, defaultValue,
				0, 0, null, null, false);
	}

	public static ConfigId<Double> floatSlider(Category category, String key, double defaultValue,
			double min, double max, SliderFormat format) {
		return new ConfigId<>(category, key, Kind.DOUBLE_SLIDER, defaultValue,
				min, max, format, null, false);
	}

	public static ConfigId<String> stringCycle(Category category, String key, String defaultValue,
			Supplier<List<String>> values, boolean allowEmpty) {
		return new ConfigId<>(category, key, Kind.STRING_CYCLE, defaultValue,
				0, 0, null, values, allowEmpty);
	}

	public static <E extends Enum<E>> ConfigId<E> enumCycle(Category category, String key,
			E defaultValue) {
		return new ConfigId<>(category, key, Kind.ENUM_CYCLE, defaultValue,
				0, 0, null, null, false);
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
}
