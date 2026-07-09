package cromveil.combatnumbers.forge.impl.platform;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

import cromveil.combatnumbers.platform.IPlatformServerLifecycle;

public class ForgeServerLifecycle implements IPlatformServerLifecycle {

	private volatile MinecraftServer server;

	@Override
	public void onServerStarted(Consumer<MinecraftServer> listener) {
		MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent e) -> {
			this.server = e.getServer();
			listener.accept(this.server);
		});
	}

	@Override
	public void onServerStopping(Consumer<MinecraftServer> listener) {
		MinecraftForge.EVENT_BUS.addListener((ServerStoppingEvent e) -> {
			listener.accept(this.server);
			this.server = null;
		});
	}

	@Override
	public void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener) {
		MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
			if (e.getEntity() instanceof ServerPlayer player && this.server != null) {
				listener.accept(player, this.server);
			}
		});
	}
}
