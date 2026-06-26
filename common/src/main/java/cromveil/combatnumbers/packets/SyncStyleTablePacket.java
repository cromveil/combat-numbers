package cromveil.combatnumbers.packets;

import cromveil.combatnumbers.styles.StyleTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SyncStyleTablePacket(
		List<Identifier> skinIds,
		List<Identifier> animationIds) implements CustomPacketPayload {

	public static final Type<SyncStyleTablePacket> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath("combatnumbers", "sync_style_table"));

	private static final StreamCodec<ByteBuf, List<Identifier>> ID_LIST_CODEC = Identifier.STREAM_CODEC
			.apply(ByteBufCodecs.list());

	public static final StreamCodec<ByteBuf, SyncStyleTablePacket> STREAM_CODEC = StreamCodec.composite(
			ID_LIST_CODEC, SyncStyleTablePacket::skinIds,
			ID_LIST_CODEC, SyncStyleTablePacket::animationIds,
			SyncStyleTablePacket::new);

	public SyncStyleTablePacket(StyleTable table) {
		this(table.skinIds(), table.animationIds());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
