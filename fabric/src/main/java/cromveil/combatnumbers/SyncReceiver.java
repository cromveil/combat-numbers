package cromveil.combatnumbers;

import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.ISetup;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SyncReceiver implements ISetup {

	private final AnimationResolver animationResolver;
	private final SkinResolver skinResolver;
	private StyleTable styleTable = StyleTable.EMPTY;
	public SyncReceiver(AnimationResolver animationResolver, SkinResolver skinResolver) {
		this.animationResolver = animationResolver;
		this.skinResolver = skinResolver;
	}

	@Override
	public void register() {

		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(SyncStyleTablePacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> styleTable = new StyleTable(
									packet.skinIds().stream().map(StableIdMapper::from).toList(),
									packet.animationIds().stream().map(StableIdMapper::from).toList())));

			ClientPlayNetworking.registerReceiver(SyncAnimationDataPacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> animationResolver.setServer(packet.animations())));

			ClientPlayNetworking.registerReceiver(SyncSkinDataPacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> skinResolver.setServerSkinDefs(packet.skins())));

			ClientPlayNetworking.registerReceiver(SyncSpriteTexturePacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> skinResolver.setServerTextureBytes(packet.textures())));
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			animationResolver.clearServer();
			skinResolver.clearServer();
			styleTable = StyleTable.EMPTY;
		});
	}

	public StyleTable styleTable() {
		return styleTable;
	}
}
