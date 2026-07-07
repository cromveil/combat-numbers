package cromveil.combatnumbers.packets;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.StableIdMapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SyncStyleTablePacket(
		List<ResourceLocation> skinIds,
		List<ResourceLocation> animationIds) implements CustomPacketPayload {

	public static final Type<SyncStyleTablePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_style_table"));

	private static final StreamCodec<ByteBuf, List<ResourceLocation>> ID_LIST_CODEC = ResourceLocation.STREAM_CODEC
			.apply(ByteBufCodecs.list());

	public static final StreamCodec<ByteBuf, SyncStyleTablePacket> STREAM_CODEC = StreamCodec.composite(
			ID_LIST_CODEC, SyncStyleTablePacket::skinIds,
			ID_LIST_CODEC, SyncStyleTablePacket::animationIds,
			SyncStyleTablePacket::new);

	public SyncStyleTablePacket(StyleTable table) {
		this(table.skinIds().stream().map(StableIdMapper::to).toList(),
				table.animationIds().stream().map(StableIdMapper::to).toList());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
