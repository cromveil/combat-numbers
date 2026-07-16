package cromveil.combatnumbers.packets;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.modules.server.StatsSnapshot;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StatsResponsePacket(
		List<TypeEntry> damageTypes,
		List<TypeEntry> damageTags,
		List<SourceEntry> sources) implements CustomPacketPayload {

	public static final Type<StatsResponsePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stats_response"));

	public static final StreamCodec<ByteBuf, StatsResponsePacket> STREAM_CODEC = StreamCodec.of(
			StatsResponsePacket::write, StatsResponsePacket::read);

	public record TypeEntry(ResourceLocation key, int count) {
		static final StreamCodec<ByteBuf, TypeEntry> STREAM_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC, TypeEntry::key,
				ByteBufCodecs.VAR_INT, TypeEntry::count,
				TypeEntry::new);
	}

	public record SourceEntry(String key, int count) {
		static final StreamCodec<ByteBuf, SourceEntry> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, SourceEntry::key,
				ByteBufCodecs.VAR_INT, SourceEntry::count,
				SourceEntry::new);
	}

	private static final StreamCodec<ByteBuf, List<TypeEntry>> TYPE_LIST_CODEC =
			TypeEntry.STREAM_CODEC.apply(ByteBufCodecs.list());
	private static final StreamCodec<ByteBuf, List<SourceEntry>> SOURCE_LIST_CODEC =
			SourceEntry.STREAM_CODEC.apply(ByteBufCodecs.list());

	private static void write(ByteBuf buf, StatsResponsePacket packet) {
		TYPE_LIST_CODEC.encode(buf, packet.damageTypes);
		TYPE_LIST_CODEC.encode(buf, packet.damageTags);
		SOURCE_LIST_CODEC.encode(buf, packet.sources);
	}

	private static StatsResponsePacket read(ByteBuf buf) {
		List<TypeEntry> types = new ArrayList<>(TYPE_LIST_CODEC.decode(buf));
		List<TypeEntry> tags = new ArrayList<>(TYPE_LIST_CODEC.decode(buf));
		List<SourceEntry> sources = new ArrayList<>(SOURCE_LIST_CODEC.decode(buf));
		return new StatsResponsePacket(types, tags, sources);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static StatsResponsePacket from(StatsSnapshot snapshot) {
		return new StatsResponsePacket(
				snapshot.damageTypes().stream()
						.map(e -> new TypeEntry(StableIdMapper.to(e.key()), e.count()))
						.toList(),
				snapshot.damageTags().stream()
						.map(e -> new TypeEntry(StableIdMapper.to(e.key()), e.count()))
						.toList(),
				snapshot.sources().stream()
						.map(e -> new SourceEntry(e.key(), e.count()))
						.toList());
	}

	public StatsSnapshot toSnapshot() {
		return new StatsSnapshot(
				damageTypes.stream()
						.map(e -> new StatsSnapshot.Entry(StableIdMapper.from(e.key()), e.count()))
						.toList(),
				damageTags.stream()
						.map(e -> new StatsSnapshot.Entry(StableIdMapper.from(e.key()), e.count()))
						.toList(),
				sources.stream()
						.map(e -> new StatsSnapshot.SourceEntry(e.key(), e.count()))
						.toList());
	}
}
