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
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncAnimationDataPacket(
		Map<StableId, Timeline> animations) implements CustomPacketPayload {

	public static final Type<SyncAnimationDataPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_animation_data"));

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, Timeline>> RAW_CODEC = ByteBufCodecs
			.map(
					LinkedHashMap::new,
					ResourceLocation.STREAM_CODEC,
					ByteBufCodecs.fromCodecWithRegistriesTrusted(TimelineCodec.CODEC));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncAnimationDataPacket> STREAM_CODEC =
			StableIdMapper.stableIdMapCodec(RAW_CODEC, SyncAnimationDataPacket::animations, SyncAnimationDataPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
