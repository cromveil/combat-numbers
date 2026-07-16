package cromveil.combatnumbers.packets;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.modules.server.StatsSnapshot;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestStatsPacket(
		ResourceLocation typeFilter,
		ResourceLocation tagFilter,
		String sourceFilter) implements CustomPacketPayload {

	public static final Type<RequestStatsPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "request_stats"));

	public static final StreamCodec<ByteBuf, RequestStatsPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC)
					.map(opt -> opt.orElse(null), rl -> java.util.Optional.ofNullable(rl)),
			RequestStatsPacket::typeFilter,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC)
					.map(opt -> opt.orElse(null), rl -> java.util.Optional.ofNullable(rl)),
			RequestStatsPacket::tagFilter,
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
					.map(opt -> opt.orElse(null), s -> java.util.Optional.ofNullable(s)),
			RequestStatsPacket::sourceFilter,
			RequestStatsPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
