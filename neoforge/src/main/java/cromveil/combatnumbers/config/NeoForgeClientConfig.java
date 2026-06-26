package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.client.theme.ThemeManager;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class NeoForgeClientConfig {

	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.BooleanValue ENABLED;
	public static final ModConfigSpec.ConfigValue<String> THEME;
	public static final ModConfigSpec.EnumValue<RenderOption> RENDER_MODE;
	public static final ModConfigSpec.DoubleValue BASE_FONT_SIZE;
	public static final ModConfigSpec.DoubleValue NEAR_FADE_DISTANCE;
	public static final ModConfigSpec.DoubleValue MAX_RENDER_DISTANCE;
	public static final ModConfigSpec.DoubleValue DISTANCE_FALLOFF_START;
	public static final ModConfigSpec.DoubleValue DISTANCE_FALLOFF_END;
	public static final ModConfigSpec.DoubleValue DISTANCE_MIN_SCALE;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		// NOTE: NeoForge auto discovers tooltips via `${translation_key}.tooltip`.
		// Different from Cloth Config / Auto Config's `${translation_key}.@Tooltip`.
		// Ignoring `.comment()` for now as I don't want a third dupe.

		ENABLED = builder
				.translation("text.autoconfig.combatnumbers-client.option.enabled")
				.define("enabled", true);

		List<String> themeOptions = new ArrayList<>();
		themeOptions.add("");
		for (String id : ThemeManager.builtinThemeIds()) {
			if (!themeOptions.contains(id)) {
				themeOptions.add(id);
			}
		}
		if (!themeOptions.contains("default")) {
			themeOptions.add("default");
		}

		THEME = builder
				.translation("text.autoconfig.combatnumbers-client.option.theme")
				.defineInList("theme", "maple", themeOptions);

		RENDER_MODE = builder
				.translation("text.autoconfig.combatnumbers-client.option.renderMode")
				.defineEnum("render_mode", RenderOption.HUD);

		BASE_FONT_SIZE = builder
				.translation("text.autoconfig.combatnumbers-client.option.baseFontSize")
				.defineInRange("base_font_size", 8.0, 0.1, 128.0);

		NEAR_FADE_DISTANCE = builder
				.translation("text.autoconfig.combatnumbers-client.option.nearFadeDistance")
				.defineInRange("near_fade_distance", 1.5, 0.0, 64.0);

		MAX_RENDER_DISTANCE = builder
				.translation("text.autoconfig.combatnumbers-client.option.maxRenderDistance")
				.defineInRange("max_render_distance", 32.0, 0.0, 256.0);

		DISTANCE_FALLOFF_START = builder
				.translation("text.autoconfig.combatnumbers-client.option.distanceFalloffStart")
				.defineInRange("distance_falloff_start", 3.0, 0.0, 256.0);

		DISTANCE_FALLOFF_END = builder
				.translation("text.autoconfig.combatnumbers-client.option.distanceFalloffEnd")
				.defineInRange("distance_falloff_end", 32.0, 0.0, 256.0);

		DISTANCE_MIN_SCALE = builder
				.translation("text.autoconfig.combatnumbers-client.option.distanceMinScale")
				.defineInRange("distance_min_scale", 0.3, 0.0, 1.0);

		SPEC = builder.build();
	}

	private NeoForgeClientConfig() {
	}
}
