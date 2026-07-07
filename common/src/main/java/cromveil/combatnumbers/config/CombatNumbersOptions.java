package cromveil.combatnumbers.config;

import cromveil.combatnumbers.config.screen.ConfigOption;
import cromveil.combatnumbers.config.screen.ConfigScreen;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.config.IConfigWriter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Stream;

public final class CombatNumbersOptions {

	private CombatNumbersOptions() {}

	public static Screen createScreen(Screen parent, IConfigState state, IConfigWriter writer) {
		return new ConfigScreen(parent,
				Component.translatable("config.combatnumbers.title"),
				"config.combatnumbers",
				() -> clientOptions(state),
				writer);
	}

	public static List<ConfigOption<?>> clientOptions(IConfigState state) {
		return Stream.concat(Configs.COMMON.stream(), Configs.CLIENT.stream())
				.<ConfigOption<?>>map(id -> ConfigWidgetFactory.toWidget(id, state))
				.toList();
	}
}
