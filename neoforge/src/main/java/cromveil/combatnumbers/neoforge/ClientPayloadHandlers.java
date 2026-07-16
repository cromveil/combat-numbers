package cromveil.combatnumbers.neoforge;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.client.ClientStatsCache;
import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.StatsScreen;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.StatsResponsePacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

	static void onStatsResponse(StatsResponsePacket payload, IPayloadContext context) {
		context.enqueueWork(() -> {
			ClientStatsCache.current = payload;
			Minecraft mc = Minecraft.getInstance();
			if (mc.screen instanceof StatsScreen s)
				s.refreshFromCache();
			else
				mc.setScreen(new StatsScreen());
		});
	}

	public static StyleTable styleTable() {
		if (styleTable == null) return StyleTable.EMPTY;
		return styleTable;
	}
}
