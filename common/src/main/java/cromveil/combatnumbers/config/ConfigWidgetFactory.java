package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.theme.ThemeManager;
import cromveil.combatnumbers.config.screen.ConfigOption;
import cromveil.combatnumbers.core.config.ConfigId;
import cromveil.combatnumbers.core.config.ConfigStore;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public final class ConfigWidgetFactory {

	private ConfigWidgetFactory() {}

	@SuppressWarnings("unchecked")
	public static ConfigOption<?> toWidget(ConfigId<?> id, ConfigStore store) {
		return switch (id.kind()) {
			case BOOL -> {
				ConfigId<Boolean> self = (ConfigId<Boolean>) (Object) id;
				yield ConfigOption.ofBool(id.key(), self.defaultValue(),
						() -> store.get(self), v -> store.set(self, v));
			}
			case DOUBLE_SLIDER -> {
				ConfigId<Double> self = (ConfigId<Double>) (Object) id;
				yield ConfigOption.ofSlider(id.key(), self.defaultValue(), id.min(), id.max(),
						() -> store.get(self), v -> store.set(self, v),
						id.sliderFormat());
			}
			case STRING_CYCLE -> {
				ConfigId<String> self = (ConfigId<String>) (Object) id;
				List<String> values = id.allowedValuesSupplier().get();
				Function<String, Component> disp = Component::literal;
				Function<Object, Component> descFn = null;
				if (id == ConfigIds.CLIENT_THEME) {
					disp = ThemeManager::displayName;
					descFn = current -> {
						String currentId = (String) current;
						if (currentId == null || currentId.isEmpty()) {
							return Component.translatable("config.combatnumbers.option.theme.off.description");
						}
						String desc = ThemeManager.description(currentId);
						return desc != null ? Component.literal(desc) : null;
					};
				}
				yield ConfigOption.ofStringCycle(id.key(), self.defaultValue(),
						() -> store.get(self), v -> store.set(self, v),
						values, disp, descFn, id.allowEmpty(),
						Component.translatable("options.off"));
			}
			case ENUM_CYCLE -> {
				ConfigOption<?> opt = ConfigOption.ofEnum(id.key(), (Enum) id.defaultValue(),
						() -> (Enum) store.get((ConfigId) (Object) id),
						v -> store.set((ConfigId) (Object) id, v),
						e -> Component.translatable(
								"config.combatnumbers.renderOption." + e.name()));
				yield opt;
			}
		};
	}
}
