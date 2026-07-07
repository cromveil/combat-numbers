package cromveil.combatnumbers;

import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class SyncReceiver implements IClientSetup {

	private final IEventBus modEventBus;
	private final AnimationResolver animationResolver;
	private final SkinResolver skinResolver;
	private StyleTable styleTable = StyleTable.EMPTY;
	public SyncReceiver(IEventBus modEventBus, AnimationResolver animationResolver, SkinResolver skinResolver) {
		this.modEventBus = modEventBus;
		this.animationResolver = animationResolver;
		this.skinResolver = skinResolver;
	}

	@Override
	public void register() {

		modEventBus.addListener(RegisterClientPayloadHandlersEvent.class, e -> {
			e.register(SyncStyleTablePacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> styleTable = new StyleTable(
							payload.skinIds().stream().map(StableIdMapper::from).toList(),
							payload.animationIds().stream().map(StableIdMapper::from).toList())));

			e.register(SyncAnimationDataPacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> animationResolver.setServer(payload.animations())));

			e.register(SyncSkinDataPacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> skinResolver.setServerSkinDefs(payload.skins())));

			e.register(SyncSpriteTexturePacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> skinResolver.setServerTextureBytes(payload.textures())));
		});

		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> {
			animationResolver.clearServer();
			skinResolver.clearServer();
			styleTable = StyleTable.EMPTY;
		});
	}

	public StyleTable styleTable() {
		return styleTable;
	}
}
