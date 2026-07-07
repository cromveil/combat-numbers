package cromveil.combatnumbers.platform;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoForgeNetwork implements PlatformNetwork {

	private final IEventBus modEventBus;

	public NeoForgeNetwork(IEventBus modEventBus) {
		this.modEventBus = modEventBus;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void registerClientbound(CustomPacketPayload.Type type, StreamCodec codec) {
		modEventBus.addListener(RegisterPayloadHandlersEvent.class, e -> {
			PayloadRegistrar registrar = e.registrar(cromveil.combatnumbers.core.Constants.MOD_ID);
			registrar.playToClient(type, codec);
		});
	}

	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
		player.connection.send(packet);
	}
}
