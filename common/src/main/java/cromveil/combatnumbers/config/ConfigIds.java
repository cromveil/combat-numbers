package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.client.theme.ThemeManager;
import cromveil.combatnumbers.config.screen.SliderFormat;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ConfigIds {

	private ConfigIds() {}

	public static final ConfigId<Boolean> ENABLED =
			ConfigId.bool(ConfigId.Category.COMMON, "enabled", true);

	public static final ConfigId<String> CLIENT_THEME =
			ConfigId.stringCycle(ConfigId.Category.CLIENT, "theme", "maple",
					ThemeManager::themeIds, ThemeManager::displayName, true);

	public static final ConfigId<RenderOption> RENDER_MODE =
			ConfigId.enumCycle(ConfigId.Category.CLIENT, "renderMode", RenderOption.HUD,
					m -> Component.translatable("config.combatnumbers.renderOption." + m.name()));

	public static final ConfigId<Double> BASE_FONT_SIZE =
			ConfigId.floatSlider(ConfigId.Category.CLIENT, "baseFontSize", 9.0,
					0.1, 32.0, SliderFormat.ONE_DECIMAL);

	public static final ConfigId<Double> NEAR_FADE_DISTANCE =
			ConfigId.floatSlider(ConfigId.Category.CLIENT, "nearFadeDistance", 1.5,
					0.0, 64.0, SliderFormat.ONE_DECIMAL);

	public static final ConfigId<Double> MAX_RENDER_DISTANCE =
			ConfigId.floatSlider(ConfigId.Category.COMMON, "maxRenderDistance", 32.0,
					0.0, 256.0, SliderFormat.INTEGER);

	public static final ConfigId<Double> DISTANCE_FALLOFF_START =
			ConfigId.floatSlider(ConfigId.Category.CLIENT, "distanceFalloffStart", 3.0,
					0.0, 256.0, SliderFormat.INTEGER);

	public static final ConfigId<Double> DISTANCE_FALLOFF_END =
			ConfigId.floatSlider(ConfigId.Category.CLIENT, "distanceFalloffEnd", 32.0,
					0.0, 256.0, SliderFormat.INTEGER);

	public static final ConfigId<Double> DISTANCE_MIN_SCALE =
			ConfigId.floatSlider(ConfigId.Category.CLIENT, "distanceMinScale", 0.3,
					0.0, 1.0, SliderFormat.TWO_DECIMALS);

	public static final List<ConfigId<?>> ALL_COMMON = List.of(
			ENABLED, MAX_RENDER_DISTANCE
	);

	public static final List<ConfigId<?>> ALL_CLIENT = List.of(
			CLIENT_THEME, RENDER_MODE,
			BASE_FONT_SIZE, NEAR_FADE_DISTANCE,
			DISTANCE_FALLOFF_START, DISTANCE_FALLOFF_END, DISTANCE_MIN_SCALE
	);

	// public static final List<ConfigId<?>> ALL_SERVER = List.of();
}
