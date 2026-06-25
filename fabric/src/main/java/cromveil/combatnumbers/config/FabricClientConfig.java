package cromveil.combatnumbers.config;

import cromveil.combatnumbers.client.render.RenderOption;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "combatnumbers-client")
public class FabricClientConfig implements ConfigData {

	public boolean enabled = true;

	@ConfigEntry.Gui.Tooltip(count = 4)
	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	public RenderOption renderMode = RenderOption.HUD;

	@ConfigEntry.Gui.Tooltip(count = 1)
	public float baseFontSize = 8.0f;

	@ConfigEntry.Gui.Tooltip(count = 1)
	public float nearFadeDistance = 1.5f;

	@ConfigEntry.Gui.Tooltip(count = 1)
	public float maxRenderDistance = 32.0f;

	@ConfigEntry.Gui.Tooltip(count = 1)
	public float distanceFalloffStart = 3.0f;

	@ConfigEntry.Gui.Tooltip(count = 1)
	public float distanceFalloffEnd = 32.0f;

	@ConfigEntry.Gui.Tooltip(count = 1)
	public float distanceMinScale = 0.3f;
}
