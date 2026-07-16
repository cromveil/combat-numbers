package cromveil.combatnumbers.fabric;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RequestStatsPacket;
import cromveil.combatnumbers.packets.StatsResponsePacket;
import cromveil.combatnumbers.client.ClientStatsCache;
import cromveil.combatnumbers.skins.SkinRegistry;

import cromveil.combatnumbers.fabric.impl.config.ConfigFiles;
import cromveil.combatnumbers.fabric.impl.platform.FabricNetwork;
import cromveil.combatnumbers.fabric.impl.platform.FabricServerLifecycle;
import cromveil.combatnumbers.fabric.impl.resource.FabricReloadRegistry;

import cromveil.combatnumbers.modules.server.BroadcastModule;
import cromveil.combatnumbers.modules.server.DamageStatsTracker;
import cromveil.combatnumbers.modules.server.EntityLevelResolver;
import cromveil.combatnumbers.modules.server.ReadDatapacksModule;
import cromveil.combatnumbers.modules.server.StatsCommand;
import cromveil.combatnumbers.modules.server.StatsSnapshot;
import cromveil.combatnumbers.modules.server.StylingModule;
import cromveil.combatnumbers.modules.server.SyncToClientModule;

public class CombatNumbers implements ModInitializer {

	@Override
	public void onInitialize() {
		var network = new FabricNetwork();
		var lifecycle = new FabricServerLifecycle();
		var reloadRegistry = new FabricReloadRegistry();

		ClientStatsCache.network = network;

		var config = ConfigFiles.load("combatnumbers-common.json", Configs.COMMON);
		var animationRegistry = new AnimationRegistry();
		var skinRegistry = new SkinRegistry();
		var ruleEngine = new RuleEngine<ServerLevel>();
		var filterRegistry = new FilterRegistry<ServerLevel>();
		Supplier<StyleTable> styleTable = () -> StyleTable.from(ruleEngine);

		var entityResolver = new EntityLevelResolver(lifecycle);
		var statsTracker = new DamageStatsTracker(entityResolver);
		var datapacks = new ReadDatapacksModule(reloadRegistry, animationRegistry, skinRegistry, ruleEngine, filterRegistry);
		var syncToClient = new SyncToClientModule(network, lifecycle, entityResolver, animationRegistry, skinRegistry, styleTable);
		var styling = new StylingModule(config, ruleEngine, filterRegistry, entityResolver);
		var broadcast = new BroadcastModule(config, network, styleTable, entityResolver);

		PayloadTypeRegistry.playC2S().register(RequestStatsPacket.TYPE,
				RequestStatsPacket.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(RequestStatsPacket.TYPE,
				(packet, ctx) -> ctx.server().execute(() -> {
					ServerPlayer player = ctx.player();
					Optional<StableId> typeFilter = packet.typeFilter() != null
							? Optional.of(StableIdMapper.from(packet.typeFilter()))
							: Optional.empty();
					Optional<StableId> tagFilter = packet.tagFilter() != null
							? Optional.of(StableIdMapper.from(packet.tagFilter()))
							: Optional.empty();
					Optional<String> sourceFilter = Optional.ofNullable(packet.sourceFilter());
					StatsSnapshot snapshot = statsTracker.getSnapshot(
							typeFilter, tagFilter, sourceFilter);
					network.sendToPlayer(player, StatsResponsePacket.from(snapshot));
				}));
		PayloadTypeRegistry.playS2C().register(StatsResponsePacket.TYPE,
				StatsResponsePacket.STREAM_CODEC);

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> StatsCommand
						.register(dispatcher, statsTracker, network));

		IServerSetup.registerAll(
				statsTracker,
				entityResolver,
				datapacks,
				styling,
				syncToClient, broadcast
		);
	}
}
