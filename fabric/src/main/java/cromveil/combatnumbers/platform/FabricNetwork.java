package cromveil.combatnumbers.platform;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetwork implements PlatformNetwork {

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void registerClientbound(CustomPacketPayload.Type type, StreamCodec codec) {
		PayloadTypeRegistry.clientboundPlay().register(type, codec);
	}

	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
		ServerPlayNetworking.send(player, packet);
	}
}
