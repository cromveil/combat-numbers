package cromveil.combatnumbers.config.screen;

import cromveil.combatnumbers.config.SliderFormat;
import cromveil.combatnumbers.core.config.ConfigDef;
import cromveil.combatnumbers.core.config.ConfigWriter;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ConfigOption<T> {

	public enum Type { BOOLEAN, CYCLE, SLIDER }

	private final ConfigDef<T> def;
	private final Type type;
	private final Supplier<T> reader;

	private T pendingValue;

	final List<T> cycleValues;
	final Function<T, Component> displayFn;
	final T emptyValue;
	final Component emptyDisplay;
	final Function<Object, Component> descriptionFn;

	final double sliderMin;
	final double sliderMax;
	final SliderFormat sliderFormat;

	private ConfigOption(ConfigDef<T> def, Type type, Supplier<T> reader,
			List<T> cycleValues, Function<T, Component> displayFn,
			T emptyValue, Component emptyDisplay,
			Function<Object, Component> descriptionFn,
			double sliderMin, double sliderMax, SliderFormat sliderFormat) {
		this.def = def;
		this.type = type;
		this.reader = reader;
		this.cycleValues = cycleValues != null ? Collections.unmodifiableList(cycleValues) : null;
		this.displayFn = displayFn;
		this.emptyValue = emptyValue;
		this.emptyDisplay = emptyDisplay;
		this.descriptionFn = descriptionFn;
		this.sliderMin = sliderMin;
		this.sliderMax = sliderMax;
		this.sliderFormat = sliderFormat;
	}

	public static ConfigOption<Boolean> ofBool(ConfigDef<Boolean> def, Supplier<Boolean> reader) {
		return new ConfigOption<>(def, Type.BOOLEAN, reader,
				null, null, null, null, null, 0, 0, null);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> ConfigOption<E> ofEnum(ConfigDef<E> def,
			Supplier<E> reader, Function<E, Component> displayFn) {
		E[] constants = (E[]) def.defaultValue().getClass().getEnumConstants();
		return new ConfigOption<>(def, Type.CYCLE, reader,
				List.of(constants), displayFn, null, null, null, 0, 0, null);
	}

	public static ConfigOption<String> ofStringCycle(ConfigDef<String> def,
			Supplier<String> reader,
			List<String> values, Function<String, Component> displayFn,
			boolean allowEmpty, Component emptyDisplay) {
		return ofStringCycle(def, reader, values, displayFn, null, allowEmpty, emptyDisplay);
	}

	public static ConfigOption<String> ofStringCycle(ConfigDef<String> def,
			Supplier<String> reader,
			List<String> values, Function<String, Component> displayFn,
			Function<Object, Component> descriptionFn,
			boolean allowEmpty, Component emptyDisplay) {
		List<String> allValues;
		if (allowEmpty) {
			allValues = new ArrayList<>(values.size() + 1);
			allValues.add("");
			allValues.addAll(values);
		} else {
			allValues = new ArrayList<>(values);
		}
		return new ConfigOption<>(def, Type.CYCLE, reader,
				allValues, displayFn, allowEmpty ? "" : null, emptyDisplay,
				descriptionFn, 0, 0, null);
	}

	public static ConfigOption<Double> ofSlider(ConfigDef<Double> def,
			Supplier<Double> reader, SliderFormat format) {
		return new ConfigOption<>(def, Type.SLIDER, reader,
				null, null, null, null, null,
				def.min(), def.max(), format);
	}

	public String key() { return def.key(); }

	public Type type() { return type; }

	public T defaultValue() { return def.defaultValue(); }

	public T get() { return pendingValue; }

	public void set(T value) { this.pendingValue = value; }

	public void load() { this.pendingValue = reader.get(); }

	public void apply(ConfigWriter writer) {
		writer.setValue(def, pendingValue);
	}

	public boolean isAtDefault() {
		if (pendingValue instanceof Double d && def.defaultValue() instanceof Double defVal) {
			return Math.abs(d - defVal) < 0.001;
		}
		return Objects.equals(pendingValue, def.defaultValue());
	}

	public void reset() {
		this.pendingValue = def.defaultValue();
	}

	public Component label(String prefix) {
		return Component.translatable(prefix + ".option." + def.key());
	}

	public Component tooltip(String prefix) {
		return Component.translatable(prefix + ".option." + def.key() + ".tooltip");
	}

	public Function<Object, Component> description() {
		return descriptionFn;
	}
}
