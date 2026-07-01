package cromveil.combatnumbers;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import cromveil.combatnumbers.config.CombatNumbersOptions;

public class CombatNumbersModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return CombatNumbersOptions::createScreen;
	}
}
