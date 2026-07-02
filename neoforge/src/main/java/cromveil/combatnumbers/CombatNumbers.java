package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.AnimationRegistry;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.config.NeoForgeConfig;
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
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MOD_ID)
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
			PayloadRegistrar registrar = e.registrar(Constants.MOD_ID);
			registrar.playToClient(RenderPacket.TYPE, RenderPacket.STREAM_CODEC);
			registrar.playToClient(SyncAnimationDataPacket.TYPE, SyncAnimationDataPacket.STREAM_CODEC);
			registrar.playToClient(SyncSkinDataPacket.TYPE, SyncSkinDataPacket.STREAM_CODEC);
			registrar.playToClient(SyncSpriteTexturePacket.TYPE, SyncSpriteTexturePacket.STREAM_CODEC);
			registrar.playToClient(SyncStyleTablePacket.TYPE, SyncStyleTablePacket.STREAM_CODEC);
		});

		NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, e -> {
			e.addListener(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animations"), animationRegistry);

			RuleLoader ruleLoader = new RuleLoader(ruleEngine);
			ruleLoader.setOnReload(() -> {
				this.styleTable = StyleTable.from(ruleEngine);
				broadcast(new SyncStyleTablePacket(this.styleTable));
			});
			e.addListener(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "styles"), ruleLoader);

			e.addListener(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "skins"), skinRegistry);

			FilterLoader filterLoader = new FilterLoader(filterRegistry);
			e.addListener(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "filters"), filterLoader);
		});

		NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e -> this.server = e.getServer());
		NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, e -> this.server = null);

		NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
			if (!(e.getEntity() instanceof ServerPlayer player))
				return;

			player.connection.send(new SyncStyleTablePacket(this.styleTable));
			player.connection.send(new SyncAnimationDataPacket(animationRegistry.getAll()));
			var texPacket = skinRegistry.buildTexturePacket();
			if (texPacket != null) {
				player.connection.send(texPacket);
			}
			player.connection.send(new SyncSkinDataPacket(skinRegistry.getAll()));
		});

		CombatNumbersEvents.COMBAT.register(event -> {
			if (!filterRegistry.passes(event))
				return;
			Style style = ruleEngine.resolve(event);
			CombatNumbersEvents.RENDER.invoker().onEvent(RenderEvent.from(event, style));
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
				player.connection.send(packet);
			}
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
			player.connection.send(packet);
		}
	}
}
