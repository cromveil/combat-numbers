package cromveil.combatnumbers.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NeoForgeServerConfig {

	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.BooleanValue ENABLED;
	public static final ModConfigSpec.DoubleValue MAX_RENDER_DISTANCE;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		// NOTE: NeoForge auto discovers tooltips via `${translation_key}.tooltip`.
		// Different from Cloth Config / Auto Config's `${translation_key}.@Tooltip`.
		// Ignoring `.comment()` for now as I don't want a third dupe.

		ENABLED = builder
				.translation("text.autoconfig.combatnumbers-server.option.enabled")
				.define("enabled", true);

		MAX_RENDER_DISTANCE = builder
				.translation("text.autoconfig.combatnumbers-server.option.maxRenderDistance")
				.defineInRange("max_render_distance", 32.0, 0.0, 256.0);

		SPEC = builder.build();
	}

	private NeoForgeServerConfig() {
	}
}
