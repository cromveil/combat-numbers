package cromveil.combatnumbers.forge.impl.platform;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.PacketDistributor;

import cromveil.combatnumbers.platform.IPlatformNetwork;

public class ForgeNetwork implements IPlatformNetwork {

	private SimpleChannel channel;

	public void setChannel(SimpleChannel channel) {
		this.channel = channel;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void registerClientbound(CustomPacketPayload.Type type, StreamCodec codec) {
	}

	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
		channel.send(packet, PacketDistributor.PLAYER.with(player));
	}
}
