package cromveil.combatnumbers.neoforge;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.client.ClientStatsCache;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.filters.FilterRegistry;
import cromveil.combatnumbers.core.styles.RuleEngine;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.RequestStatsPacket;
import cromveil.combatnumbers.packets.StatsResponsePacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.skins.SkinRegistry;

import cromveil.combatnumbers.neoforge.impl.config.ConfigFiles;
import cromveil.combatnumbers.neoforge.impl.platform.NeoForgeNetwork;
import cromveil.combatnumbers.neoforge.impl.platform.NeoForgeServerLifecycle;
import cromveil.combatnumbers.neoforge.impl.resource.NeoForgeReloadRegistry;

import cromveil.combatnumbers.modules.server.BroadcastModule;
import cromveil.combatnumbers.modules.server.EntityLevelResolver;
import cromveil.combatnumbers.modules.server.ReadDatapacksModule;
import cromveil.combatnumbers.modules.server.StylingModule;
import cromveil.combatnumbers.modules.server.SyncToClientModule;
import cromveil.combatnumbers.modules.server.DamageStatsTracker;
import cromveil.combatnumbers.modules.server.StatsCommand;
import cromveil.combatnumbers.modules.server.StatsSnapshot;

@Mod(Constants.MOD_ID)
public class CombatNumbers {

	private DamageStatsTracker statsTracker;

	public CombatNumbers(IEventBus modEventBus, ModContainer container) {
		var network = new NeoForgeNetwork(modEventBus);
		var lifecycle = new NeoForgeServerLifecycle();
		var reloadRegistry = new NeoForgeReloadRegistry(modEventBus);

		var config = ConfigFiles.of(modEventBus, container, ModConfig.Type.COMMON, Configs.COMMON);
		var animationRegistry = new AnimationRegistry();
		var skinRegistry = new SkinRegistry();
		var ruleEngine = new RuleEngine<ServerLevel>();
		var filterRegistry = new FilterRegistry<ServerLevel>();
		Supplier<StyleTable> styleTable = () -> StyleTable.from(ruleEngine);

		var entityResolver = new EntityLevelResolver(lifecycle);
		statsTracker = new DamageStatsTracker(entityResolver);
		var datapacks = new ReadDatapacksModule(reloadRegistry, animationRegistry, skinRegistry, ruleEngine, filterRegistry);
		var syncToClient = new SyncToClientModule(network, lifecycle, entityResolver, animationRegistry, skinRegistry, styleTable);
		var styling = new StylingModule(config, ruleEngine, filterRegistry, entityResolver);
		var broadcast = new BroadcastModule(config, network, styleTable, entityResolver);

		ClientStatsCache.network = network;

		modEventBus.addListener(RegisterPayloadHandlersEvent.class, e -> {
			PayloadRegistrar registrar = e.registrar(Constants.MOD_ID);
			registrar.playToClient(RenderPacket.TYPE, RenderPacket.STREAM_CODEC,
					ClientPayloadHandlers::onRender);
			registrar.playToClient(SyncAnimationDataPacket.TYPE,
					SyncAnimationDataPacket.STREAM_CODEC,
					ClientPayloadHandlers::onSyncAnimationData);
			registrar.playToClient(SyncSkinDataPacket.TYPE,
					SyncSkinDataPacket.STREAM_CODEC,
					ClientPayloadHandlers::onSyncSkinData);
			registrar.playToClient(SyncSpriteTexturePacket.TYPE,
					SyncSpriteTexturePacket.STREAM_CODEC,
					ClientPayloadHandlers::onSyncSpriteTexture);
			registrar.playToClient(SyncStyleTablePacket.TYPE,
					SyncStyleTablePacket.STREAM_CODEC,
					ClientPayloadHandlers::onSyncStyleTable);
			registrar.playToClient(StatsResponsePacket.TYPE,
					StatsResponsePacket.STREAM_CODEC,
					ClientPayloadHandlers::onStatsResponse);
			registrar.playToServer(RequestStatsPacket.TYPE,
					RequestStatsPacket.STREAM_CODEC,
					this::onStatsRequest);
		});

		NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class,
				e -> StatsCommand.register(e.getDispatcher(), statsTracker, network));

		IServerSetup.registerAll(
				statsTracker,
				entityResolver,
				datapacks,
				styling,
				syncToClient, broadcast
		);
	}

	private void onStatsRequest(RequestStatsPacket packet, IPayloadContext ctx) {
		Optional<StableId> typeFilter = packet.typeFilter() != null
				? Optional.of(StableIdMapper.from(packet.typeFilter()))
				: Optional.empty();
		Optional<StableId> tagFilter = packet.tagFilter() != null
				? Optional.of(StableIdMapper.from(packet.tagFilter()))
				: Optional.empty();
		Optional<String> sourceFilter = Optional.ofNullable(packet.sourceFilter());
		StatsSnapshot snapshot = statsTracker.getSnapshot(
				typeFilter, tagFilter, sourceFilter);
		ctx.reply(StatsResponsePacket.from(snapshot));
	}
}
