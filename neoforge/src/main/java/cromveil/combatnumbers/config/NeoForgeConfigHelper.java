package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;

public final class NeoForgeConfigHelper implements IConfigHelper {

	@Override
	public boolean clientEnabled() {
		return NeoForgeClientConfig.ENABLED.get();
	}

	@Override
	public String clientTheme() {
		return NeoForgeClientConfig.THEME.get();
	}

	@Override
	public RenderOption renderMode() {
		return NeoForgeClientConfig.RENDER_MODE.get();
	}

	@Override
	public float baseFontSize() {
		return NeoForgeClientConfig.BASE_FONT_SIZE.get().floatValue();
	}

	@Override
	public float nearFadeDistance() {
		return NeoForgeClientConfig.NEAR_FADE_DISTANCE.get().floatValue();
	}

	@Override
	public float maxRenderDistance() {
		return NeoForgeClientConfig.MAX_RENDER_DISTANCE.get().floatValue();
	}

	@Override
	public float distanceFalloffStart() {
		return NeoForgeClientConfig.DISTANCE_FALLOFF_START.get().floatValue();
	}

	@Override
	public float distanceFalloffEnd() {
		return NeoForgeClientConfig.DISTANCE_FALLOFF_END.get().floatValue();
	}

	@Override
	public float distanceMinScale() {
		return NeoForgeClientConfig.DISTANCE_MIN_SCALE.get().floatValue();
	}

	@Override
	public boolean serverEnabled() {
		return NeoForgeServerConfig.ENABLED.get();
	}

	@Override
	public float serverMaxRenderDistance() {
		return NeoForgeServerConfig.MAX_RENDER_DISTANCE.get().floatValue();
	}
}
