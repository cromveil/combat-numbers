package cromveil.combatnumbers.core.config;

import java.util.List;
import java.util.function.Supplier;

public final class ConfigDef<T> {

	public enum Category { CLIENT, SERVER, COMMON }

	public enum ValueType { BOOL, NUMBER, STRING_LIST, ENUM }

	private final Category category;
	private final String key;
	private final ValueType valueType;
	private final T defaultValue;
	private final double min, max;
	private final int decimalPlaces;
	private final Supplier<List<String>> allowedValuesSupplier;
	private final boolean allowEmpty;

	private ConfigDef(Category category, String key, ValueType valueType, T defaultValue,
			double min, double max, int decimalPlaces,
			Supplier<List<String>> allowedValuesSupplier, boolean allowEmpty) {
		this.category = category;
		this.key = key;
		this.valueType = valueType;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.decimalPlaces = decimalPlaces;
		this.allowedValuesSupplier = allowedValuesSupplier;
		this.allowEmpty = allowEmpty;
	}

	public static ConfigDef<Boolean> bool(Category category, String key, boolean defaultValue) {
		return new ConfigDef<>(category, key, ValueType.BOOL, defaultValue,
				0, 0, 0, null, false);
	}

	public static ConfigDef<Double> doubleRange(Category category, String key, double defaultValue,
			double min, double max, int decimalPlaces) {
		return new ConfigDef<>(category, key, ValueType.NUMBER, defaultValue,
				min, max, decimalPlaces, null, false);
	}

	public static ConfigDef<String> stringList(Category category, String key, String defaultValue,
			Supplier<List<String>> values, boolean allowEmpty) {
		return new ConfigDef<>(category, key, ValueType.STRING_LIST, defaultValue,
				0, 0, 0, values, allowEmpty);
	}

	public static <E extends Enum<E>> ConfigDef<E> enumValue(Category category, String key,
			E defaultValue) {
		return new ConfigDef<>(category, key, ValueType.ENUM, defaultValue,
				0, 0, 0, null, false);
	}

	public ConfigDef<Boolean> asBool() {
		if (valueType != ValueType.BOOL) throw new IllegalStateException("Not a BOOL: " + valueType);
		@SuppressWarnings("unchecked")
		ConfigDef<Boolean> r = (ConfigDef<Boolean>) (ConfigDef<?>) this;
		return r;
	}

	public ConfigDef<Double> asNumber() {
		if (valueType != ValueType.NUMBER) throw new IllegalStateException("Not a NUMBER: " + valueType);
		@SuppressWarnings("unchecked")
		ConfigDef<Double> r = (ConfigDef<Double>) (ConfigDef<?>) this;
		return r;
	}

	public ConfigDef<String> asStringList() {
		if (valueType != ValueType.STRING_LIST) throw new IllegalStateException("Not a STRING_LIST: " + valueType);
		@SuppressWarnings("unchecked")
		ConfigDef<String> r = (ConfigDef<String>) (ConfigDef<?>) this;
		return r;
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> ConfigDef<E> asEnum() {
		if (valueType != ValueType.ENUM) throw new IllegalStateException("Not an ENUM: " + valueType);
		return (ConfigDef<E>) (ConfigDef<?>) this;
	}

	public Category category() { return category; }
	public String key() { return key; }
	public ValueType valueType() { return valueType; }
	public T defaultValue() { return defaultValue; }
	public double min() { return min; }
	public double max() { return max; }
	public int decimalPlaces() { return decimalPlaces; }
	public Supplier<List<String>> allowedValuesSupplier() { return allowedValuesSupplier; }
	public boolean allowEmpty() { return allowEmpty; }
}
