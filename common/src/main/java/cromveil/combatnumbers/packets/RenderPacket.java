package cromveil.combatnumbers.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RenderPacket(
		int entityId, float value,
		int skinIndex, int animationIndex) implements CustomPacketPayload {

	// NOTE
	// skinIndex / animationIndex are indices into the StyleTable, not their
	// respective registries.

	public static final Type<RenderPacket> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath("combatnumbers", "render"));

	public static final StreamCodec<RegistryFriendlyByteBuf, RenderPacket> STREAM_CODEC = CustomPacketPayload
			.codec(RenderPacket::write, RenderPacket::new);

	private RenderPacket(RegistryFriendlyByteBuf buf) {
		this(
				buf.readVarInt(),
				buf.readFloat(),
				buf.readVarInt() - 1,
				buf.readVarInt() - 1);
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeFloat(value);
		buf.writeVarInt(skinIndex + 1); // +1 offsets so -1 (representing null) turns into 0 to save on varint size
		buf.writeVarInt(animationIndex + 1);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
