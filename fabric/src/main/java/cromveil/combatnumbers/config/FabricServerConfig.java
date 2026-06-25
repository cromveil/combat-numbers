package cromveil.combatnumbers.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "combatnumbers-server")
public class FabricServerConfig implements ConfigData {

	public boolean enabled = true;
	public float maxRenderDistance = 32.0f;
}
