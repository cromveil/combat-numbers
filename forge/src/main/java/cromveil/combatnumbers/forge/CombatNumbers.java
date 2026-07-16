package cromveil.combatnumbers.forge;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
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

import cromveil.combatnumbers.forge.impl.config.ConfigFiles;
import cromveil.combatnumbers.forge.impl.platform.ForgeNetwork;
import cromveil.combatnumbers.forge.impl.platform.ForgeServerLifecycle;
import cromveil.combatnumbers.forge.impl.resource.ForgeReloadRegistry;

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

	public CombatNumbers(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();
		ModContainer container = context.getContainer();

		var network = new ForgeNetwork();
		var lifecycle = new ForgeServerLifecycle();
		var reloadRegistry = new ForgeReloadRegistry(modEventBus);

		var commonConfig = ConfigFiles.of(modEventBus, container, ModConfig.Type.COMMON, Configs.COMMON);
		var animationRegistry = new AnimationRegistry();
		var skinRegistry = new SkinRegistry();
		var ruleEngine = new RuleEngine<ServerLevel>();
		var filterRegistry = new FilterRegistry<ServerLevel>();
		Supplier<StyleTable> styleTable = () -> StyleTable.from(ruleEngine);

		var entityResolver = new EntityLevelResolver(lifecycle);
		var statsTracker = new DamageStatsTracker(entityResolver);
		var datapacks = new ReadDatapacksModule(reloadRegistry, animationRegistry, skinRegistry, ruleEngine, filterRegistry);
		var syncToClient = new SyncToClientModule(network, lifecycle, entityResolver, animationRegistry, skinRegistry, styleTable);
		var styling = new StylingModule(commonConfig, ruleEngine, filterRegistry, entityResolver);
		var broadcast = new BroadcastModule(commonConfig, network, styleTable, entityResolver);

		ClientStatsCache.network = network;

		SimpleChannel channel = ChannelBuilder.named(Constants.MOD_ID + ":network")
				.simpleChannel();

		channel.messageBuilder(RenderPacket.class)
				.direction(PacketFlow.CLIENTBOUND)
				.codec((StreamCodec<FriendlyByteBuf, RenderPacket>) (Object) RenderPacket.STREAM_CODEC)
				.consumerMainThread((packet, ctx) -> {
					var f = ForgeClientBridge.factory();
					if (f != null) f.onRenderPacket(packet.entityId(), packet.value(),
							packet.skinIndex(), packet.animationIndex());
				})
				.add();

		channel.messageBuilder(SyncAnimationDataPacket.class)
				.direction(PacketFlow.CLIENTBOUND)
				.codec((StreamCodec<FriendlyByteBuf, SyncAnimationDataPacket>) (Object) SyncAnimationDataPacket.STREAM_CODEC)
				.consumerMainThread((packet, ctx) -> {
					var sr = ForgeClientBridge.syncReceiver();
					if (sr != null) sr.setAnimations(packet.animations());
				})
				.add();

		channel.messageBuilder(SyncSkinDataPacket.class)
				.direction(PacketFlow.CLIENTBOUND)
				.codec((StreamCodec<FriendlyByteBuf, SyncSkinDataPacket>) (Object) SyncSkinDataPacket.STREAM_CODEC)
				.consumerMainThread((packet, ctx) -> {
					var sr = ForgeClientBridge.syncReceiver();
					if (sr != null) sr.setSkins(packet.skins());
				})
				.add();

		channel.messageBuilder(SyncSpriteTexturePacket.class)
				.direction(PacketFlow.CLIENTBOUND)
				.codec((StreamCodec<FriendlyByteBuf, SyncSpriteTexturePacket>) (Object) SyncSpriteTexturePacket.STREAM_CODEC)
				.consumerMainThread((packet, ctx) -> {
					var sr = ForgeClientBridge.syncReceiver();
					if (sr != null) sr.setTextures(packet.textures());
				})
				.add();

		channel.messageBuilder(SyncStyleTablePacket.class)
				.direction(PacketFlow.CLIENTBOUND)
				.codec((StreamCodec<FriendlyByteBuf, SyncStyleTablePacket>) (Object) SyncStyleTablePacket.STREAM_CODEC)
				.consumerMainThread((packet, ctx) -> {
					var sr = ForgeClientBridge.syncReceiver();
					if (sr != null) sr.setStyleTable(new StyleTable(
							packet.skinIds().stream().map(StableIdMapper::from).toList(),
							packet.animationIds().stream().map(StableIdMapper::from).toList()));
				})
				.add();

		channel.messageBuilder(RequestStatsPacket.class)
				.direction(PacketFlow.SERVERBOUND)
				.codec((StreamCodec<FriendlyByteBuf, RequestStatsPacket>) (Object) RequestStatsPacket.STREAM_CODEC)
				.consumerMainThread((packet, ctx) -> {
					ServerPlayer player = ctx.getSender();
					if (player == null) return;
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
				})
				.add();

		channel.messageBuilder(StatsResponsePacket.class)
				.direction(PacketFlow.CLIENTBOUND)
				.codec((StreamCodec<FriendlyByteBuf, StatsResponsePacket>) (Object) StatsResponsePacket.STREAM_CODEC)
				.consumerMainThread((packet, ctx) -> {
					ClientStatsCache.current = packet;
					var mc = net.minecraft.client.Minecraft.getInstance();
					if (mc == null) return;
					mc.execute(() -> {
						if (mc.screen instanceof cromveil.combatnumbers.client.StatsScreen s)
							s.refreshFromCache();
						else
							mc.setScreen(new cromveil.combatnumbers.client.StatsScreen());
					});
				})
				.add();

		channel.build();
		network.setChannel(channel);

		IServerSetup.registerAll(
				statsTracker,
				entityResolver,
				datapacks,
				styling,
				syncToClient, broadcast
		);

		MinecraftForge.EVENT_BUS.addListener(
				(RegisterCommandsEvent e) -> StatsCommand.register(
						e.getDispatcher(), statsTracker, network));

		if (FMLEnvironment.dist == Dist.CLIENT) {
			CombatNumbersClient.setup(modEventBus, container, network, commonConfig);
		}
	}
}
