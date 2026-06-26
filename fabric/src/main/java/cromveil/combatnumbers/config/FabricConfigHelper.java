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
	public boolean serverEnabled() {
		return server().enabled;
	}

	@Override
	public float serverMaxRenderDistance() {
		return server().maxRenderDistance;
	}
}
