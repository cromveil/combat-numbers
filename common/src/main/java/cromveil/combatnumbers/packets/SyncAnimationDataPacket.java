package cromveil.combatnumbers.packets;

import java.util.LinkedHashMap;
import java.util.Map;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.resource.StableIdMapper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

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
			StreamCodec.of(
					(buf, packet) -> {
						Map<Identifier, Timeline> raw = new LinkedHashMap<>();
						packet.animations().forEach((k, v) -> raw.put(StableIdMapper.to(k), v));
						RAW_CODEC.encode(buf, raw);
					},
					buf -> {
						Map<Identifier, Timeline> raw = RAW_CODEC.decode(buf);
						Map<StableId, Timeline> map = new LinkedHashMap<>();
						raw.forEach((k, v) -> map.put(StableIdMapper.from(k), v));
						return new SyncAnimationDataPacket(map);
					});

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
