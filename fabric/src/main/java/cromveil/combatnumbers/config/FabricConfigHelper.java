package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;
import me.shedaniel.autoconfig.AutoConfig;

public final class FabricConfigHelper implements IConfigHelper {

	private static FabricClientConfig client() {
		return AutoConfig.getConfigHolder(FabricClientConfig.class).getConfig();
	}

	private static FabricServerConfig server() {
		return AutoConfig.getConfigHolder(FabricServerConfig.class).getConfig();
	}

	@Override
	public boolean clientEnabled() {
		return client().enabled;
	}

	@Override
	public String clientTheme() {
		return client().theme;
	}

	@Override
	public RenderOption renderMode() {
		return client().renderMode;
	}

	@Override
	public float baseFontSize() {
		return client().baseFontSize;
	}

	@Override
	public float nearFadeDistance() {
		return client().nearFadeDistance;
	}

	@Override
	public float maxRenderDistance() {
		return client().maxRenderDistance;
	}

	@Override
	public float distanceFalloffStart() {
		return client().distanceFalloffStart;
	}

	@Override
	public float distanceFalloffEnd() {
		return client().distanceFalloffEnd;
	}

	@Override
	public float distanceMinScale() {
		return client().distanceMinScale;
	}

	@Override
	public void setClientEnabled(boolean enabled) {
		var config = client();
		config.enabled = enabled;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setClientTheme(String theme) {
		var config = client();
		config.theme = theme;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setClientRenderMode(RenderOption mode) {
		var config = client();
		config.renderMode = mode;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setBaseFontSize(float size) {
		var config = client();
		config.baseFontSize = size;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setNearFadeDistance(float distance) {
		var config = client();
		config.nearFadeDistance = distance;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setClientMaxRenderDistance(float distance) {
		var config = client();
		config.maxRenderDistance = distance;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setDistanceFalloffStart(float distance) {
		var config = client();
		config.distanceFalloffStart = distance;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setDistanceFalloffEnd(float distance) {
		var config = client();
		config.distanceFalloffEnd = distance;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public void setDistanceMinScale(float scale) {
		var config = client();
		config.distanceMinScale = scale;
		AutoConfig.getConfigHolder(FabricClientConfig.class).save();
	}

	@Override
	public boolean serverEnabled() {
		return server().enabled;
	}

	@Override
	public float serverMaxRenderDistance() {
		return server().maxRenderDistance;
	}
}
