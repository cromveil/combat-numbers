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
	public void setClientEnabled(boolean enabled) {
		NeoForgeClientConfig.ENABLED.set(enabled);
		NeoForgeClientConfig.ENABLED.save();
	}

	@Override
	public void setClientTheme(String theme) {
		NeoForgeClientConfig.THEME.set(theme);
		NeoForgeClientConfig.THEME.save();
	}

	@Override
	public void setClientRenderMode(RenderOption mode) {
		NeoForgeClientConfig.RENDER_MODE.set(mode);
		NeoForgeClientConfig.RENDER_MODE.save();
	}

	@Override
	public void setBaseFontSize(float size) {
		NeoForgeClientConfig.BASE_FONT_SIZE.set((double) size);
		NeoForgeClientConfig.BASE_FONT_SIZE.save();
	}

	@Override
	public void setNearFadeDistance(float distance) {
		NeoForgeClientConfig.NEAR_FADE_DISTANCE.set((double) distance);
		NeoForgeClientConfig.NEAR_FADE_DISTANCE.save();
	}

	@Override
	public void setClientMaxRenderDistance(float distance) {
		NeoForgeClientConfig.MAX_RENDER_DISTANCE.set((double) distance);
		NeoForgeClientConfig.MAX_RENDER_DISTANCE.save();
	}

	@Override
	public void setDistanceFalloffStart(float distance) {
		NeoForgeClientConfig.DISTANCE_FALLOFF_START.set((double) distance);
		NeoForgeClientConfig.DISTANCE_FALLOFF_START.save();
	}

	@Override
	public void setDistanceFalloffEnd(float distance) {
		NeoForgeClientConfig.DISTANCE_FALLOFF_END.set((double) distance);
		NeoForgeClientConfig.DISTANCE_FALLOFF_END.save();
	}

	@Override
	public void setDistanceMinScale(float scale) {
		NeoForgeClientConfig.DISTANCE_MIN_SCALE.set((double) scale);
		NeoForgeClientConfig.DISTANCE_MIN_SCALE.save();
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
