package cromveil.combatnumbers.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricServerLifecycle implements PlatformServerLifecycle {

	@Override
	public void onServerStarted(Consumer<MinecraftServer> listener) {
		ServerLifecycleEvents.SERVER_STARTED.register(listener::accept);
	}

	@Override
	public void onServerStopping(Consumer<MinecraftServer> listener) {
		ServerLifecycleEvents.SERVER_STOPPING.register(listener::accept);
	}

	@Override
	public void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener) {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				listener.accept(handler.getPlayer(), server));
	}
}
