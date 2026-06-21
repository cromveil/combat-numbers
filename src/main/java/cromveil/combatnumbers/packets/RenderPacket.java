package cromveil.combatnumbers.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RenderPacket(
		int entityId, float value,
		int skinIndex, int animationIndex, float scale
) implements CustomPacketPayload {

	public static final Type<RenderPacket> TYPE =
		new Type<>(Identifier.fromNamespaceAndPath("combatnumbers", "render"));

	public static final StreamCodec<RegistryFriendlyByteBuf, RenderPacket> STREAM_CODEC =
		CustomPacketPayload.codec(RenderPacket::write, RenderPacket::new);

	private RenderPacket(RegistryFriendlyByteBuf buf) {
		this(
			buf.readVarInt(),
			buf.readFloat(),
			buf.readVarInt(),
			buf.readVarInt(),
			buf.readFloat()
		);
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeFloat(value);
		buf.writeVarInt(skinIndex);
		buf.writeVarInt(animationIndex);
		buf.writeFloat(scale);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
