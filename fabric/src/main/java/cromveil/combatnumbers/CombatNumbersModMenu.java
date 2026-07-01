package cromveil.combatnumbers;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import cromveil.combatnumbers.config.CombatNumbersOptions;
import cromveil.combatnumbers.config.Config;

public class CombatNumbersModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> CombatNumbersOptions.createScreen(screen, Config.store());
	}
}
