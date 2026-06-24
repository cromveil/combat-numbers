package cromveil.combatnumbers.packets;

import java.util.LinkedHashMap;
import java.util.Map;

import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.animation.codec.TimelineCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncAnimationDataPacket(
		Map<Identifier, Timeline> animations) implements CustomPacketPayload {

	public static final Type<SyncAnimationDataPacket> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath("combatnumbers", "sync_animation_data"));

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, Timeline>> ANIM_MAP_STREAM_CODEC = ByteBufCodecs
			.map(
					LinkedHashMap::new,
					Identifier.STREAM_CODEC,
					ByteBufCodecs.fromCodecWithRegistriesTrusted(TimelineCodec.CODEC));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncAnimationDataPacket> STREAM_CODEC = ANIM_MAP_STREAM_CODEC
			.map(SyncAnimationDataPacket::new, SyncAnimationDataPacket::animations);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
