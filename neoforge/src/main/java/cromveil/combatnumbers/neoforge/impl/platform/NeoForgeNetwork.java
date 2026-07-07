package cromveil.combatnumbers.neoforge.impl.platform;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.bus.api.IEventBus;

import cromveil.combatnumbers.platform.IPlatformNetwork;

public class NeoForgeNetwork implements IPlatformNetwork {

	private final IEventBus modEventBus;

	public NeoForgeNetwork(IEventBus modEventBus) {
		this.modEventBus = modEventBus;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void registerClientbound(CustomPacketPayload.Type type, StreamCodec codec) {
	}

	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
		player.connection.send(packet);
	}
}
