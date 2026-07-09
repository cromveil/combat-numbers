package cromveil.combatnumbers.forge.impl.platform;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

import cromveil.combatnumbers.platform.IPlatformServerLifecycle;

public class ForgeServerLifecycle implements IPlatformServerLifecycle {

	private volatile MinecraftServer server;

	@Override
	public void onServerStarted(Consumer<MinecraftServer> listener) {
		ServerStartedEvent.BUS.addListener(e -> {
			this.server = e.getServer();
			listener.accept(this.server);
		});
	}

	@Override
	public void onServerStopping(Consumer<MinecraftServer> listener) {
		ServerStoppingEvent.BUS.addListener(e -> {
			listener.accept(this.server);
			this.server = null;
		});
	}

	@Override
	public void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener) {
		PlayerEvent.PlayerLoggedInEvent.BUS.addListener(e -> {
			if (e.getEntity() instanceof ServerPlayer player && this.server != null) {
				listener.accept(player, this.server);
			}
		});
	}
}
