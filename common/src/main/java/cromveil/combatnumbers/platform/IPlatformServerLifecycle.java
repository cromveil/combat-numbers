package cromveil.combatnumbers.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IPlatformServerLifecycle {

	void onServerStarted(Consumer<MinecraftServer> listener);

	void onServerStopping(Consumer<MinecraftServer> listener);

	void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener);
}
