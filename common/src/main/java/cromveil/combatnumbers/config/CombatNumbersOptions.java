package cromveil.combatnumbers.config;

import cromveil.combatnumbers.config.screen.ConfigOption;
import cromveil.combatnumbers.config.screen.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Stream;

public final class CombatNumbersOptions {

	private CombatNumbersOptions() {}

	public static Screen createScreen(Screen parent, ConfigStore store) {
		return new ConfigScreen(parent,
				Component.translatable("config.combatnumbers.title"),
				"config.combatnumbers",
				() -> clientOptions(store),
				store);
	}

	public static List<ConfigOption<?>> clientOptions(ConfigStore store) {
		return Stream.concat(ConfigIds.ALL_COMMON.stream(), ConfigIds.ALL_CLIENT.stream())
				.<ConfigOption<?>>map(id -> id.toOption(store))
				.toList();
	}
}
