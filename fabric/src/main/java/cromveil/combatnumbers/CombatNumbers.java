package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.config.FabricConfig;
import cromveil.combatnumbers.events.CombatNumbersEvents;
import cromveil.combatnumbers.events.RenderEvent;
import cromveil.combatnumbers.filters.FilterLoader;
import cromveil.combatnumbers.filters.FilterRegistry;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.skins.SkinRegistry;
import cromveil.combatnumbers.styles.RuleEngine;
import cromveil.combatnumbers.styles.RuleLoader;
import cromveil.combatnumbers.styles.Style;
import cromveil.combatnumbers.styles.StyleTable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;

public class CombatNumbers implements ModInitializer {
	private MinecraftServer server;
	private StyleTable styleTable = StyleTable.EMPTY;

	@Override
	public void onInitialize() {
		Config.init(new FabricConfig());
		PayloadTypeRegistry.clientboundPlay()
				.register(RenderPacket.TYPE, RenderPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay()
				.register(SyncAnimationDataPacket.TYPE, SyncAnimationDataPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay()
				.register(SyncSkinDataPacket.TYPE, SyncSkinDataPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay()
				.register(SyncSpriteTexturePacket.TYPE, SyncSpriteTexturePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay()
				.register(SyncStyleTablePacket.TYPE, SyncStyleTablePacket.STREAM_CODEC);

		ServerLifecycleEvents.SERVER_STARTED.register(x -> this.server = x);
		ServerLifecycleEvents.SERVER_STOPPING.register(x -> this.server = null);

		var animationRegistry = new AnimationRegistry();
		var skinRegistry = new SkinRegistry();
		var ruleEngine = new RuleEngine();
		var ruleLoader = new RuleLoader(ruleEngine);
		var filterRegistry = new FilterRegistry();
		var filterLoader = new FilterLoader(filterRegistry);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "animations"), animationRegistry);
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "styles"), ruleLoader);
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "skins"), skinRegistry);
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "filters"), filterLoader);

		CombatNumbersEvents.COMBAT.register(event -> {
			if (!filterRegistry.passes(event))
				return;
			Style style = ruleEngine.resolve(event);
			CombatNumbersEvents.RENDER.invoker().onEvent(RenderEvent.from(event, style));
		});

		ruleLoader.setOnReload(() -> {
			this.styleTable = StyleTable.from(ruleEngine);
			broadcast(new SyncStyleTablePacket(this.styleTable));
		});

		CombatNumbersEvents.RENDER.register((instance) -> {
			LivingEntity entity = instance.entity();
			RenderPacket packet = new RenderPacket(
					entity.getId(), instance.value(),
					this.styleTable.skinIndex(instance.skinId()),
					this.styleTable.animationIndex(instance.animationId()));

			ServerLevel level = (ServerLevel) entity.level();
			double entityX = entity.getX();
			double entityY = entity.getY();
			double entityZ = entity.getZ();

			double maxDistSq = Config.get(ConfigIds.MAX_RENDER_DISTANCE);
			maxDistSq *= maxDistSq;
			for (ServerPlayer player : level.players()) {
				if (player.distanceToSqr(entityX, entityY, entityZ) > maxDistSq)
					continue;
				ServerPlayNetworking.send(player, packet);
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, _server) -> {
			ServerPlayer player = handler.getPlayer();
			ServerPlayNetworking.send(player, new SyncStyleTablePacket(this.styleTable));
			ServerPlayNetworking.send(player, new SyncAnimationDataPacket(animationRegistry.getAll()));
			var texPacket = skinRegistry.buildTexturePacket();
			if (texPacket != null) {
				ServerPlayNetworking.send(player, texPacket);
			}
			ServerPlayNetworking.send(player, new SyncSkinDataPacket(skinRegistry.getAll()));
		});

		animationRegistry.setOnReload(() ->
				broadcast(new SyncAnimationDataPacket(animationRegistry.getAll())));

		skinRegistry.setOnReload(() -> {
			var texPacket = skinRegistry.buildTexturePacket();
			if (texPacket != null) {
				broadcast(texPacket);
			}
			broadcast(new SyncSkinDataPacket(skinRegistry.getAll()));
		});
	}

	private void broadcast(CustomPacketPayload packet) {
		if (this.server == null)
			return;
		for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(player, packet);
		}
	}
}
