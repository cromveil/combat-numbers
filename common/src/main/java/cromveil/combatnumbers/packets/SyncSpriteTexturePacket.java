package cromveil.combatnumbers.packets;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncSpriteTexturePacket(
	Map<Identifier, byte[]> textures
) implements CustomPacketPayload {

	public static final Type<SyncSpriteTexturePacket> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath("combatnumbers", "sync_sprite_texture"));

	private static final StreamCodec<RegistryFriendlyByteBuf, byte[]> BYTE_ARRAY_CODEC =
		new StreamCodec<>() {
			public byte[] decode(RegistryFriendlyByteBuf buf) {
				int len = buf.readVarInt();
				byte[] data = new byte[len];
				buf.readBytes(data);
				return data;
			}
			public void encode(RegistryFriendlyByteBuf buf, byte[] value) {
				buf.writeVarInt(value.length);
				buf.writeBytes(value);
			}
		};

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, byte[]>> TEXTURE_MAP_CODEC =
		StreamCodec.of(
			(buf, map) -> {
				buf.writeVarInt(map.size());
				for (var entry : map.entrySet()) {
					Identifier.STREAM_CODEC.encode(buf, entry.getKey());
					BYTE_ARRAY_CODEC.encode(buf, entry.getValue());
				}
			},
			buf -> {
				int size = buf.readVarInt();
				Map<Identifier, byte[]> map = new LinkedHashMap<>();
				for (int i = 0; i < size; i++) {
					Identifier key = Identifier.STREAM_CODEC.decode(buf);
					byte[] value = BYTE_ARRAY_CODEC.decode(buf);
					map.put(key, value);
				}
				return map;
			}
		);

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncSpriteTexturePacket> STREAM_CODEC =
		TEXTURE_MAP_CODEC.map(SyncSpriteTexturePacket::new, SyncSpriteTexturePacket::textures);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
