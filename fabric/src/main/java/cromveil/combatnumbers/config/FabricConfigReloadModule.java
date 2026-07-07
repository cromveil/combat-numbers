package cromveil.combatnumbers.config;

import cromveil.combatnumbers.core.Setup;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class FabricConfigReloadModule implements Setup {

	@Override
	public void register() {
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			ConfigFiles.reloadAll();
		});
	}
}
