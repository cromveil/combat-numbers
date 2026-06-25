package cromveil.combatnumbers;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import cromveil.combatnumbers.config.FabricClientConfig;
import me.shedaniel.autoconfig.AutoConfigClient;

public class CombatNumbersModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfigClient.getConfigScreen(FabricClientConfig.class, parent).get();
	}
}
