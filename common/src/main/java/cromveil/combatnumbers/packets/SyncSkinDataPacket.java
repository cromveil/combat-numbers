package cromveil.combatnumbers.packets;

import java.util.LinkedHashMap;
import java.util.Map;

import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncSkinDataPacket(
		Map<Identifier, SkinDefinition> skins
) implements CustomPacketPayload {

	public static final Type<SyncSkinDataPacket> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath("combatnumbers", "sync_skin_data"));

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, SkinDefinition>> SKIN_MAP_STREAM_CODEC =
		ByteBufCodecs.map(
			LinkedHashMap::new,
			Identifier.STREAM_CODEC,
			ByteBufCodecs.fromCodecWithRegistriesTrusted(SkinDefinition.CODEC)
		);

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkinDataPacket> STREAM_CODEC =
		SKIN_MAP_STREAM_CODEC.map(SyncSkinDataPacket::new, SyncSkinDataPacket::skins);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
