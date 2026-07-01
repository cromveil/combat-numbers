package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.client.theme.ThemeManager;
import cromveil.combatnumbers.config.screen.ConfigOption;
import cromveil.combatnumbers.config.screen.ConfigScreen;
import cromveil.combatnumbers.config.screen.SliderFormat;
import cromveil.combatnumbers.platform.Services;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class CombatNumbersOptions {

	private CombatNumbersOptions() {}

	public static Screen createScreen(Screen parent) {
		return new ConfigScreen(parent,
				Component.translatable("config.combatnumbers.title"),
				"config.combatnumbers",
				CombatNumbersOptions::clientOptions,
				null);
	}

	public static List<ConfigOption<?>> clientOptions() {
		return List.of(
				ConfigOption.ofBool("enabled", true,
						() -> Services.CONFIG.clientEnabled(),
						v -> Services.CONFIG.setClientEnabled(v)),

				ConfigOption.ofStringCycle("theme", "maple",
						() -> Services.CONFIG.clientTheme(),
						v -> Services.CONFIG.setClientTheme(v),
						ThemeManager.builtinThemeIds(),
						Component::literal,
						true, Component.translatable("options.off")),

				ConfigOption.ofEnum("renderMode", RenderOption.HUD,
						() -> Services.CONFIG.renderMode(),
						v -> Services.CONFIG.setClientRenderMode(v),
						m -> Component.translatable("config.combatnumbers.renderOption." + m.name())),

				ConfigOption.ofSlider("baseFontSize", 8.0f, 0.1f, 32.0f,
						() -> Services.CONFIG.baseFontSize(),
						v -> Services.CONFIG.setBaseFontSize(v),
						SliderFormat.ONE_DECIMAL),

				ConfigOption.ofSlider("nearFadeDistance", 1.5f, 0.0f, 64.0f,
						() -> Services.CONFIG.nearFadeDistance(),
						v -> Services.CONFIG.setNearFadeDistance(v),
						SliderFormat.ONE_DECIMAL),

				ConfigOption.ofSlider("maxRenderDistance", 32.0f, 0.0f, 256.0f,
						() -> Services.CONFIG.maxRenderDistance(),
						v -> Services.CONFIG.setClientMaxRenderDistance(v),
						SliderFormat.INTEGER),

				ConfigOption.ofSlider("distanceFalloffStart", 3.0f, 0.0f, 256.0f,
						() -> Services.CONFIG.distanceFalloffStart(),
						v -> Services.CONFIG.setDistanceFalloffStart(v),
						SliderFormat.INTEGER),

				ConfigOption.ofSlider("distanceFalloffEnd", 32.0f, 0.0f, 256.0f,
						() -> Services.CONFIG.distanceFalloffEnd(),
						v -> Services.CONFIG.setDistanceFalloffEnd(v),
						SliderFormat.INTEGER),

				ConfigOption.ofSlider("distanceMinScale", 0.3f, 0.0f, 1.0f,
						() -> Services.CONFIG.distanceMinScale(),
						v -> Services.CONFIG.setDistanceMinScale(v),
						SliderFormat.TWO_DECIMALS)
		);
	}
}
