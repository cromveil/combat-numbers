package cromveil.combatnumbers.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NeoForgeServerLifecycle implements PlatformServerLifecycle {

	private volatile MinecraftServer server;

	@Override
	public void onServerStarted(Consumer<MinecraftServer> listener) {
		NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e -> {
			this.server = e.getServer();
			listener.accept(this.server);
		});
	}

	@Override
	public void onServerStopping(Consumer<MinecraftServer> listener) {
		NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, e -> {
			listener.accept(this.server);
			this.server = null;
		});
	}

	@Override
	public void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener) {
		NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
			if (e.getEntity() instanceof ServerPlayer player && this.server != null) {
				listener.accept(player, this.server);
			}
		});
	}
}
