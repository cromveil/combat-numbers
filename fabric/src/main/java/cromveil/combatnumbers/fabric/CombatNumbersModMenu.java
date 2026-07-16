package cromveil.combatnumbers.fabric;

import java.util.Map;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import cromveil.combatnumbers.config.CombatNumbersOptions;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.fabric.impl.config.ConfigFiles;
import cromveil.combatnumbers.core.config.MergedConfig;

public class CombatNumbersModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			var common = ConfigFiles.load("combatnumbers-common.json", Configs.COMMON);
			var client = ConfigFiles.load("combatnumbers-client.json", Configs.CLIENT);
			var config = new MergedConfig(Map.of(
					Category.COMMON, common,
					Category.CLIENT, client));
			return CombatNumbersOptions.createScreen(screen, config, config);
		};
	}
}
