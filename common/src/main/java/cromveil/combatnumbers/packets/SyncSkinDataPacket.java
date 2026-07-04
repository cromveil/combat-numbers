package cromveil.combatnumbers.packets;

import java.util.LinkedHashMap;
import java.util.Map;

import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.resource.ResourceIds;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncSkinDataPacket(
		Map<ResourceId, SkinDefinition> skins
) implements CustomPacketPayload {

	public static final Type<SyncSkinDataPacket> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath("combatnumbers", "sync_skin_data"));

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, SkinDefinition>> RAW_CODEC =
		ByteBufCodecs.map(
			LinkedHashMap::new,
			Identifier.STREAM_CODEC,
			ByteBufCodecs.fromCodecWithRegistriesTrusted(SkinDefinition.CODEC)
		);

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkinDataPacket> STREAM_CODEC =
		StreamCodec.of(
			(buf, packet) -> {
				Map<Identifier, SkinDefinition> raw = new LinkedHashMap<>();
				packet.skins().forEach((k, v) -> raw.put(ResourceIds.to(k), v));
				RAW_CODEC.encode(buf, raw);
			},
			buf -> {
				Map<Identifier, SkinDefinition> raw = RAW_CODEC.decode(buf);
				Map<ResourceId, SkinDefinition> map = new LinkedHashMap<>();
				raw.forEach((k, v) -> map.put(ResourceIds.from(k), v));
				return new SyncSkinDataPacket(map);
			});

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
