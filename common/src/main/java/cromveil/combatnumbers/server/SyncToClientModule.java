package cromveil.combatnumbers.server;

import java.util.function.Supplier;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.core.ISetup;
import cromveil.combatnumbers.core.events.CombatNumbersEvents;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.platform.IPlatformNetwork;
import cromveil.combatnumbers.platform.IPlatformServerLifecycle;
import cromveil.combatnumbers.skins.SkinRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class SyncToClientModule implements ISetup {

	private final IPlatformNetwork network;
	private final IPlatformServerLifecycle lifecycle;
	private final EntityLevelResolver entities;
	private final AnimationRegistry animationRegistry;
	private final SkinRegistry skinRegistry;
	private final Supplier<StyleTable> styleTable;

	public SyncToClientModule(IPlatformNetwork network,
			IPlatformServerLifecycle lifecycle,
			EntityLevelResolver entities,
			AnimationRegistry animationRegistry,
			SkinRegistry skinRegistry,
			Supplier<StyleTable> styleTable) {
		this.network = network;
		this.lifecycle = lifecycle;
		this.entities = entities;
		this.animationRegistry = animationRegistry;
		this.skinRegistry = skinRegistry;
		this.styleTable = styleTable;
	}

	@Override
	public void register() {
		registerPackets();
		CombatNumbersEvents.DATA_RELOADED.register(this::syncAll);
		lifecycle.onPlayerJoin((player, _server) -> syncToPlayer(player));
	}

	private void syncAll() {
		syncToAll(new SyncStyleTablePacket(styleTable.get()));
		syncToAll(new SyncAnimationDataPacket(animationRegistry.getAll()));
		var texPacket = skinRegistry.buildTexturePacket();
		if (texPacket != null) {
			syncToAll(texPacket);
		}
		syncToAll(new SyncSkinDataPacket(skinRegistry.getAll()));
	}

	private void syncToPlayer(ServerPlayer player) {
		network.sendToPlayer(player, new SyncStyleTablePacket(styleTable.get()));
		network.sendToPlayer(player, new SyncAnimationDataPacket(animationRegistry.getAll()));
		var texPacket = skinRegistry.buildTexturePacket();
		if (texPacket != null) {
			network.sendToPlayer(player, texPacket);
		}
		network.sendToPlayer(player, new SyncSkinDataPacket(skinRegistry.getAll()));
	}

	private void syncToAll(CustomPacketPayload packet) {
		MinecraftServer server = entities.server();
		if (server == null)
			return;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			network.sendToPlayer(player, packet);
		}
	}

	private void registerPackets() {
		network.registerClientbound(SyncAnimationDataPacket.TYPE, SyncAnimationDataPacket.STREAM_CODEC);
		network.registerClientbound(SyncSkinDataPacket.TYPE, SyncSkinDataPacket.STREAM_CODEC);
		network.registerClientbound(SyncSpriteTexturePacket.TYPE, SyncSpriteTexturePacket.STREAM_CODEC);
		network.registerClientbound(SyncStyleTablePacket.TYPE, SyncStyleTablePacket.STREAM_CODEC);
	}
}
