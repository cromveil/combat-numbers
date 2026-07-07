package cromveil.combatnumbers.neoforge;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;

public class ClientPayloadHandlers {

	private static AnimationResolver animationResolver;
	private static SkinResolver skinResolver;
	private static StyleTable styleTable;
	private static FloatingTextFactory floatingTextFactory;

	static void init(AnimationResolver animResolver, SkinResolver skinRes,
			FloatingTextFactory factory) {
		animationResolver = animResolver;
		skinResolver = skinRes;
		floatingTextFactory = factory;
	}

	static void onSyncStyleTable(SyncStyleTablePacket payload, IPayloadContext context) {
		context.enqueueWork(() -> styleTable = new StyleTable(
				payload.skinIds().stream().map(StableIdMapper::from).toList(),
				payload.animationIds().stream().map(StableIdMapper::from).toList()));
	}

	static void onSyncAnimationData(SyncAnimationDataPacket payload, IPayloadContext context) {
		context.enqueueWork(() -> animationResolver.setServer(payload.animations()));
	}

	static void onSyncSkinData(SyncSkinDataPacket payload, IPayloadContext context) {
		context.enqueueWork(() -> skinResolver.setServerSkinDefs(payload.skins()));
	}

	static void onSyncSpriteTexture(SyncSpriteTexturePacket payload, IPayloadContext context) {
		context.enqueueWork(() -> skinResolver.setServerTextureBytes(payload.textures()));
	}

	static void onRender(RenderPacket payload, IPayloadContext context) {
		context.enqueueWork(() -> floatingTextFactory.onRenderPacket(
				payload.entityId(), payload.value(),
				payload.skinIndex(), payload.animationIndex()));
	}

	public static StyleTable styleTable() {
		if (styleTable == null) return StyleTable.EMPTY;
		return styleTable;
	}
}
