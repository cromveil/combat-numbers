package cromveil.combatnumbers.config.screen;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ConfigOption<T> {

	public enum Type { BOOLEAN, CYCLE, SLIDER }

	private final String key;
	private final Type type;
	private final T defaultValue;
	private final Supplier<T> externalReader;
	private final Consumer<T> externalWriter;

	private T pendingValue;

	final List<T> cycleValues;
	final Function<T, Component> displayFn;
	final T emptyValue;
	final Component emptyDisplay;

	final double sliderMin;
	final double sliderMax;
	final SliderFormat sliderFormat;

	private ConfigOption(String key, Type type, T defaultValue,
			Supplier<T> externalReader, Consumer<T> externalWriter,
			List<T> cycleValues, Function<T, Component> displayFn,
			T emptyValue, Component emptyDisplay,
			double sliderMin, double sliderMax, SliderFormat sliderFormat) {
		this.key = key;
		this.type = type;
		this.defaultValue = defaultValue;
		this.externalReader = externalReader;
		this.externalWriter = externalWriter;
		this.cycleValues = cycleValues != null ? Collections.unmodifiableList(cycleValues) : null;
		this.displayFn = displayFn;
		this.emptyValue = emptyValue;
		this.emptyDisplay = emptyDisplay;
		this.sliderMin = sliderMin;
		this.sliderMax = sliderMax;
		this.sliderFormat = sliderFormat;
	}

	public static ConfigOption<Boolean> ofBool(String key, boolean defaultValue,
			Supplier<Boolean> reader, Consumer<Boolean> writer) {
		return new ConfigOption<>(key, Type.BOOLEAN, defaultValue, reader, writer,
				null, null, null, null, 0, 0, null);
	}

	public static <E extends Enum<E>> ConfigOption<E> ofEnum(String key, E defaultValue,
			Supplier<E> reader, Consumer<E> writer, Function<E, Component> displayFn) {
		@SuppressWarnings("unchecked")
		E[] constants = (E[]) defaultValue.getClass().getEnumConstants();
		return new ConfigOption<>(key, Type.CYCLE, defaultValue, reader, writer,
				List.of(constants), displayFn, null, null, 0, 0, null);
	}

	public static ConfigOption<String> ofStringCycle(String key, String defaultValue,
			Supplier<String> reader, Consumer<String> writer,
			List<String> values, Function<String, Component> displayFn,
			boolean allowEmpty, Component emptyDisplay) {
		List<String> allValues;
		if (allowEmpty) {
			allValues = new ArrayList<>(values.size() + 1);
			allValues.add("");
			allValues.addAll(values);
		} else {
			allValues = new ArrayList<>(values);
		}
		return new ConfigOption<>(key, Type.CYCLE, defaultValue, reader, writer,
				allValues, displayFn, allowEmpty ? "" : null, emptyDisplay, 0, 0, null);
	}

	public static ConfigOption<Double> ofSlider(String key, double defaultValue,
			double min, double max, Supplier<Double> reader, Consumer<Double> writer,
			SliderFormat format) {
		return new ConfigOption<>(key, Type.SLIDER, defaultValue, reader, writer,
				null, null, null, null, min, max, format);
	}

	public String key() { return key; }

	public Type type() { return type; }

	public T defaultValue() { return defaultValue; }

	public T get() { return pendingValue; }

	public void set(T value) { this.pendingValue = value; }

	public void load() { this.pendingValue = externalReader.get(); }

	public void save() { externalWriter.accept(pendingValue); }

	public boolean isAtDefault() {
		if (pendingValue instanceof Double d && defaultValue instanceof Double def) {
			return Math.abs(d - def) < 0.001;
		}
		return Objects.equals(pendingValue, defaultValue);
	}

	public void reset() {
		this.pendingValue = defaultValue;
	}

	public Component label(String prefix) {
		return Component.translatable(prefix + ".option." + key);
	}

	public Component tooltip(String prefix) {
		return Component.translatable(prefix + ".option." + key + ".tooltip");
	}
}
