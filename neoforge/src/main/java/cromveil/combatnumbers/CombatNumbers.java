package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.config.NeoForgeConfig;
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
import cromveil.combatnumbers.resource.NeoForgeReloadRegistry;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.skins.SkinRegistry;
import cromveil.combatnumbers.styles.RuleProcessor;
import cromveil.combatnumbers.styles.RuleSet;
import cromveil.combatnumbers.styles.WhenCondition;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(cromveil.combatnumbers.core.Constants.MOD_ID)
public class CombatNumbers {
	private MinecraftServer server;
	private StyleTable styleTable = StyleTable.EMPTY;
	private final AnimationRegistry animationRegistry = new AnimationRegistry();
	private final SkinRegistry skinRegistry = new SkinRegistry();
	private final RuleEngine ruleEngine = new RuleEngine();
	private final FilterRegistry filterRegistry = new FilterRegistry();

	public CombatNumbers(IEventBus modEventBus, ModContainer container) {
		NeoForgeConfig config = NeoForgeConfig.instance();
		Config.init(config);

		container.registerConfig(ModConfig.Type.COMMON, config.commonSpec());

		modEventBus.addListener(RegisterPayloadHandlersEvent.class, e -> {
			PayloadRegistrar registrar = e.registrar(cromveil.combatnumbers.core.Constants.MOD_ID);
			registrar.playToClient(RenderPacket.TYPE, RenderPacket.STREAM_CODEC);
			registrar.playToClient(SyncAnimationDataPacket.TYPE,
					SyncAnimationDataPacket.STREAM_CODEC);
			registrar.playToClient(SyncSkinDataPacket.TYPE,
					SyncSkinDataPacket.STREAM_CODEC);
			registrar.playToClient(SyncSpriteTexturePacket.TYPE,
					SyncSpriteTexturePacket.STREAM_CODEC);
			registrar.playToClient(SyncStyleTablePacket.TYPE,
					SyncStyleTablePacket.STREAM_CODEC);
		});

		var reloadRegistry = new NeoForgeReloadRegistry(modEventBus);
		reloadRegistry.registerServerData(
				Identifier.fromNamespaceAndPath(cromveil.combatnumbers.core.Constants.MOD_ID, "animations"),
				"animations", TimelineCodec.CODEC,
				DataConsumer.from(animationRegistry::accept));

		var ruleProcessor = new RuleProcessor(ruleEngine);
		ruleProcessor.setOnReload(() -> {
			this.styleTable = StyleTable.from(ruleEngine);
			broadcast(new SyncStyleTablePacket(this.styleTable));
		});
		reloadRegistry.registerServerData(
				Identifier.fromNamespaceAndPath(cromveil.combatnumbers.core.Constants.MOD_ID, "styles"),
				"styles", RuleSet.CODEC,
				DataConsumer.from(ruleProcessor::accept));

		reloadRegistry.registerServerData(
				Identifier.fromNamespaceAndPath(cromveil.combatnumbers.core.Constants.MOD_ID, "skins"),
				"skins", SkinDefinition.CODEC,
				skinRegistry::accept);

		var filterProcessor = new FilterProcessor(filterRegistry);
		reloadRegistry.registerServerData(
				Identifier.fromNamespaceAndPath(cromveil.combatnumbers.core.Constants.MOD_ID, "filters"),
				"filters", WhenCondition.CODEC.listOf(),
				DataConsumer.from(filterProcessor::accept));

		NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class,
				e -> this.server = e.getServer());
		NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class,
				e -> this.server = null);

		NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
			if (!(e.getEntity() instanceof ServerPlayer player))
				return;

			player.connection.send(new SyncStyleTablePacket(this.styleTable));
			player.connection.send(
					new SyncAnimationDataPacket(animationRegistry.getAll()));
			var texPacket = skinRegistry.buildTexturePacket();
			if (texPacket != null) {
				player.connection.send(texPacket);
			}
			player.connection.send(new SyncSkinDataPacket(skinRegistry.getAll()));
		});

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
				player.connection.send(packet);
			}
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
			player.connection.send(packet);
		}
	}
}
