package cromveil.combatnumbers;

import cromveil.combatnumbers.Systems;
import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.config.FabricConfig;
import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.core.events.CombatNumbersEvents;
import cromveil.combatnumbers.core.events.RenderEvent;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.Style;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.filters.FilterProcessor;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.resource.DataConsumer;
import cromveil.combatnumbers.resource.FabricReloadRegistry;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.skins.SkinRegistry;
import cromveil.combatnumbers.styles.RuleProcessor;
import cromveil.combatnumbers.styles.RuleSet;
import cromveil.combatnumbers.styles.WhenCondition;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class CombatNumbers implements ModInitializer {
	private MinecraftServer server;
	private StyleTable styleTable = StyleTable.EMPTY;

	@Override
	public void onInitialize() {
		Config.init(new FabricConfig());
		Systems.initServer(new Systems.Server(Config.store(), CombatNumbersEvents.COMBAT, CombatNumbersEvents.RENDER));
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
		var ruleProcessor = new RuleProcessor(ruleEngine);
		var filterRegistry = new FilterRegistry();
		var filterProcessor = new FilterProcessor(filterRegistry);

		var reloadRegistry = new FabricReloadRegistry();
		reloadRegistry.registerServerData(
				ResourceId.of(cromveil.combatnumbers.core.Constants.MOD_ID, "animations"),
				"animations", TimelineCodec.CODEC,
				DataConsumer.from(animationRegistry::accept));
		reloadRegistry.registerServerData(
				ResourceId.of(cromveil.combatnumbers.core.Constants.MOD_ID, "styles"),
				"styles", RuleSet.CODEC,
				DataConsumer.from(ruleProcessor::accept));
		reloadRegistry.registerServerData(
				ResourceId.of(cromveil.combatnumbers.core.Constants.MOD_ID, "skins"),
				"skins", SkinDefinition.CODEC,
				skinRegistry::accept);
		reloadRegistry.registerServerData(
				ResourceId.of(cromveil.combatnumbers.core.Constants.MOD_ID, "filters"),
				"filters", WhenCondition.CODEC.listOf(),
				DataConsumer.from(filterProcessor::accept));

		CombatNumbersEvents.COMBAT.register(event -> {
			ServerLevel entityLevel = findEntityLevel(event.entityId());
			if (entityLevel == null)
				return;

			if (!filterRegistry.passes(event, entityLevel))
				return;
			Style style = ruleEngine.resolve(event, entityLevel);

			CombatNumbersEvents.RENDER.invoker().onEvent(
					new RenderEvent(event.entityId(), event.value(), style.skinId(), style.animationId()));
		});

		ruleProcessor.setOnReload(() -> {
			this.styleTable = StyleTable.from(ruleEngine);
			broadcast(new SyncStyleTablePacket(this.styleTable));
		});

		CombatNumbersEvents.RENDER.register(instance -> {
			int entityId = instance.entityId();
			ServerLevel level = findEntityLevel(entityId);
			if (level == null)
				return;

			var entity = level.getEntity(entityId);
			if (!(entity instanceof LivingEntity livingEntity))
				return;

			RenderPacket packet = new RenderPacket(
					entityId, instance.value(),
					this.styleTable.skinIndex(instance.skinId()),
					this.styleTable.animationIndex(instance.animationId()));

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
			ServerPlayNetworking.send(player,
					new SyncAnimationDataPacket(animationRegistry.getAll()));
			var texPacket = skinRegistry.buildTexturePacket();
			if (texPacket != null) {
				ServerPlayNetworking.send(player, texPacket);
			}
			ServerPlayNetworking.send(player,
					new SyncSkinDataPacket(skinRegistry.getAll()));
		});

		animationRegistry.setOnReload(
				() -> broadcast(new SyncAnimationDataPacket(animationRegistry.getAll())));

		skinRegistry.setOnReload(() -> {
			var texPacket = skinRegistry.buildTexturePacket();
			if (texPacket != null) {
				broadcast(texPacket);
			}
			broadcast(new SyncSkinDataPacket(skinRegistry.getAll()));
		});
	}

	private ServerLevel findEntityLevel(int entityId) {
		if (this.server == null)
			return null;
		for (ServerLevel level : this.server.getAllLevels()) {
			if (level.getEntity(entityId) != null)
				return level;
		}
		return null;
	}

	private void broadcast(CustomPacketPayload packet) {
		if (this.server == null)
			return;
		for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(player, packet);
		}
	}
}
