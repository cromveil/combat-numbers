package cromveil.combatnumbers.packets;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.StableIdMapper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncAnimationDataPacket(
		Map<StableId, Timeline> animations) implements CustomPacketPayload {

	public static final Type<SyncAnimationDataPacket> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sync_animation_data"));

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, Timeline>> RAW_CODEC = ByteBufCodecs
			.map(
					LinkedHashMap::new,
					Identifier.STREAM_CODEC,
					ByteBufCodecs.fromCodecWithRegistriesTrusted(TimelineCodec.CODEC));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncAnimationDataPacket> STREAM_CODEC =
			StableIdMapper.stableIdMapCodec(RAW_CODEC, SyncAnimationDataPacket::animations, SyncAnimationDataPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
