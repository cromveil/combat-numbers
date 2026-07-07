package cromveil.combatnumbers.server;

import cromveil.combatnumbers.core.Setup;
import cromveil.combatnumbers.platform.PlatformServerLifecycle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class EntityLevelResolver implements Setup {

	private final PlatformServerLifecycle lifecycle;
	private MinecraftServer server;
	public EntityLevelResolver(PlatformServerLifecycle lifecycle) {
		this.lifecycle = lifecycle;
	}

	@Override
	public void register() {
		lifecycle.onServerStarted(s -> server = s);
		lifecycle.onServerStopping(s -> server = null);
	}

	public MinecraftServer server() {
		return server;
	}

	public ServerLevel resolve(int entityId) {
		if (server == null)
			return null;
		for (ServerLevel level : server.getAllLevels())
			if (level.getEntity(entityId) != null)
				return level;
		return null;
	}
}
