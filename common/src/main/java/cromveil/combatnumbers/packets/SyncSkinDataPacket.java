package cromveil.combatnumbers.packets;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncSkinDataPacket(
		Map<StableId, SkinDefinition> skins
) implements CustomPacketPayload {

	public static final Type<SyncSkinDataPacket> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sync_skin_data"));

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, SkinDefinition>> RAW_CODEC =
		ByteBufCodecs.map(
			LinkedHashMap::new,
			Identifier.STREAM_CODEC,
			ByteBufCodecs.fromCodecWithRegistriesTrusted(SkinDefinition.CODEC)
		);

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkinDataPacket> STREAM_CODEC =
		StableIdMapper.stableIdMapCodec(RAW_CODEC, SyncSkinDataPacket::skins, SyncSkinDataPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
