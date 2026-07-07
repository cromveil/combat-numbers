package cromveil.combatnumbers.platform;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface IPlatformNetwork {

	@SuppressWarnings("rawtypes")
	default void registerClientbound(CustomPacketPayload.Type type, StreamCodec codec) {
	}

	void sendToPlayer(ServerPlayer player, CustomPacketPayload packet);
}
