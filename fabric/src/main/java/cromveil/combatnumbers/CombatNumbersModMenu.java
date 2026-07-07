package cromveil.combatnumbers;

import java.util.Map;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import cromveil.combatnumbers.config.CombatNumbersOptions;
import cromveil.combatnumbers.config.ConfigFiles;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.core.config.MergedConfigState;
import cromveil.combatnumbers.core.config.MergedConfigWriter;

public class CombatNumbersModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			var common = ConfigFiles.load("combatnumbers-common.json", Configs.COMMON);
			var client = ConfigFiles.load("combatnumbers-client.json", Configs.CLIENT);
			var state = new MergedConfigState(Map.of(
					Category.COMMON, common.state(),
					Category.CLIENT, client.state()));
			var writer = new MergedConfigWriter(Map.of(
					Category.COMMON, common.writer(),
					Category.CLIENT, client.writer()));
			return CombatNumbersOptions.createScreen(screen, state, writer);
		};
	}
}
