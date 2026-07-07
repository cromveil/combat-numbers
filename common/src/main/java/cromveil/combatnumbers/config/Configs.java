package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.client.theme.ThemeDiscoverer;
import cromveil.combatnumbers.core.config.ConfigDef;

import java.util.List;

public final class Configs {

	private Configs() {}

	public static final ConfigDef<Boolean> ENABLED =
			ConfigDef.bool(ConfigDef.Category.COMMON, "enabled", true);

	public static final ConfigDef<String> CLIENT_THEME =
			ConfigDef.stringList(ConfigDef.Category.CLIENT, "theme", "maple",
					ThemeDiscoverer::themeIds, true);

	public static final ConfigDef<RenderOption> RENDER_MODE =
			ConfigDef.enumValue(ConfigDef.Category.CLIENT, "renderMode", RenderOption.HUD);

	public static final ConfigDef<Double> BASE_FONT_SIZE =
			ConfigDef.doubleRange(ConfigDef.Category.CLIENT, "baseFontSize", 9.0,
					0.1, 32.0, 1);

	public static final ConfigDef<Double> NEAR_FADE_DISTANCE =
			ConfigDef.doubleRange(ConfigDef.Category.CLIENT, "nearFadeDistance", 1.5,
					0.0, 64.0, 1);

	public static final ConfigDef<Double> MAX_RENDER_DISTANCE =
			ConfigDef.doubleRange(ConfigDef.Category.COMMON, "maxRenderDistance", 32.0,
					0.0, 256.0, 0);

	public static final ConfigDef<Double> DISTANCE_FALLOFF_START =
			ConfigDef.doubleRange(ConfigDef.Category.CLIENT, "distanceFalloffStart", 3.0,
					0.0, 256.0, 0);

	public static final ConfigDef<Double> DISTANCE_FALLOFF_END =
			ConfigDef.doubleRange(ConfigDef.Category.CLIENT, "distanceFalloffEnd", 32.0,
					0.0, 256.0, 0);

	public static final ConfigDef<Double> DISTANCE_MIN_SCALE =
			ConfigDef.doubleRange(ConfigDef.Category.CLIENT, "distanceMinScale", 0.3,
					0.0, 1.0, 2);

	public static final List<ConfigDef<?>> COMMON = List.of(
			ENABLED, MAX_RENDER_DISTANCE
	);

	public static final List<ConfigDef<?>> CLIENT = List.of(
			CLIENT_THEME, RENDER_MODE,
			BASE_FONT_SIZE, NEAR_FADE_DISTANCE,
			DISTANCE_FALLOFF_START, DISTANCE_FALLOFF_END, DISTANCE_MIN_SCALE
	);
}
