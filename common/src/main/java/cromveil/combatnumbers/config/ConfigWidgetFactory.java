package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.theme.ThemeDiscoverer;
import cromveil.combatnumbers.config.screen.ConfigOption;
import cromveil.combatnumbers.core.config.ConfigDef;
import cromveil.combatnumbers.core.config.IConfigState;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public final class ConfigWidgetFactory {

	private ConfigWidgetFactory() {}

	public static ConfigOption<?> toWidget(ConfigDef<?> id, IConfigState state) {
		return switch (id.valueType()) {
			case BOOL -> {
				var self = id.asBool();
				yield ConfigOption.ofBool(self, () -> state.get(self));
			}
			case NUMBER -> {
				var self = id.asNumber();
				yield ConfigOption.ofSlider(self, () -> state.get(self),
						SliderFormat.fromDecimalPlaces(id.decimalPlaces()));
			}
			case STRING_LIST -> {
				var self = id.asStringList();
				List<String> values = id.allowedValuesSupplier().get();
				Function<String, Component> disp = Component::literal;
				Function<Object, Component> descFn = null;
				if (id == Configs.CLIENT_THEME) {
					disp = ThemeDiscoverer::displayName;
					descFn = current -> {
						String currentId = (String) current;
						if (currentId == null || currentId.isEmpty()) {
							return Component.translatable("config.combatnumbers.option.theme.off.description");
						}
						String desc = ThemeDiscoverer.description(currentId);
						return desc != null ? Component.literal(desc) : null;
					};
				}
				yield ConfigOption.ofStringCycle(self,
						() -> state.get(self),
						values, disp, descFn, id.allowEmpty(),
						Component.translatable("options.off"));
			}
			case ENUM -> {
				@SuppressWarnings("rawtypes")
				ConfigDef raw = id.asEnum();
				@SuppressWarnings({"rawtypes", "unchecked"})
				ConfigOption<?> opt = ConfigOption.ofEnum(raw,
						() -> (Enum) state.get(raw),
						e -> Component.translatable(
								"config.combatnumbers.renderOption." + e.name()));
				yield opt;
			}
		};
	}
}
