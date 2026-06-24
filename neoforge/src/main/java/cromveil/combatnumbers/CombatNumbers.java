package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.animation.codec.TimelineCodec;
import cromveil.combatnumbers.animation.registry.AnimationRegistry;
import cromveil.combatnumbers.config.ModConfig;
import cromveil.combatnumbers.events.CombatNumbersEvents;
import cromveil.combatnumbers.events.RenderEvent;
import cromveil.combatnumbers.filters.FilterLoader;
import cromveil.combatnumbers.filters.FilterRegistry;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.skins.SkinDefinitionRegistry;
import cromveil.combatnumbers.skins.SpriteSkinDefinition;
import cromveil.combatnumbers.styles.RuleEngine;
import cromveil.combatnumbers.styles.RuleLoader;
import cromveil.combatnumbers.styles.Style;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod(Constants.MOD_ID)
public class CombatNumbers {
	private MinecraftServer server;
	private final AnimationRegistry animationRegistry = new AnimationRegistry();
	private final SkinDefinitionRegistry skinDefinitions = new SkinDefinitionRegistry();
	private final RuleEngine ruleEngine = new RuleEngine();
	private final FilterRegistry filterRegistry = new FilterRegistry();

	public CombatNumbers(IEventBus modEventBus) {
		ModConfig.getInstance();

		modEventBus.addListener(RegisterPayloadHandlersEvent.class, e -> {
			PayloadRegistrar registrar = e.registrar(Constants.MOD_ID);
			registrar.playToClient(RenderPacket.TYPE, RenderPacket.STREAM_CODEC);
			registrar.playToClient(SyncAnimationDataPacket.TYPE, SyncAnimationDataPacket.STREAM_CODEC);
			registrar.playToClient(SyncSkinDataPacket.TYPE, SyncSkinDataPacket.STREAM_CODEC);
			registrar.playToClient(SyncSpriteTexturePacket.TYPE, SyncSpriteTexturePacket.STREAM_CODEC);
		});

		NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, e -> {
			e.addListener(
					Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animations"),
					new SimpleJsonResourceReloadListener<>(TimelineCodec.CODEC,
							FileToIdConverter.json("animations")) {
						@Override
						protected void apply(Map<Identifier, Timeline> entries,
								ResourceManager manager, ProfilerFiller profiler) {
							animationRegistry.reload(entries);
						}
					});

			RuleLoader ruleLoader = new RuleLoader(ruleEngine);
			e.addListener(
					Identifier.fromNamespaceAndPath(Constants.MOD_ID, "styles"),
					ruleLoader);

			e.addListener(
					Identifier.fromNamespaceAndPath(Constants.MOD_ID, "skins"),
					skinDefinitions);

			FilterLoader filterLoader = new FilterLoader(filterRegistry);
			e.addListener(
					Identifier.fromNamespaceAndPath(Constants.MOD_ID, "filters"),
					filterLoader);
		});

		NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class,
				e -> this.server = e.getServer());
		NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class,
				e -> this.server = null);
		NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class,
				e -> {
					if (!(e.getEntity() instanceof ServerPlayer player))
						return;

					var animPacket = new SyncAnimationDataPacket(animationRegistry.getAll());
					player.connection.send(animPacket);

					var texPacket = collectTexturePacket(skinDefinitions);
					if (texPacket != null) {
						player.connection.send(texPacket);
					}

					var skinPacket = new SyncSkinDataPacket(skinDefinitions.getAll());
					player.connection.send(skinPacket);
				});

		CombatNumbersEvents.COMBAT.register(event -> {
			if (!filterRegistry.passes(event))
				return;
			Style style = ruleEngine.resolve(event);
			CombatNumbersEvents.RENDER.invoker().onEvent(RenderEvent.from(event, style));
		});

		CombatNumbersEvents.RENDER.register((instance) -> {
			LivingEntity entity = instance.entity();
			int skinIndex = skinDefinitions.indexOf(instance.skinId());
			int animIndex = animationRegistry.indexOf(instance.animationId());
			RenderPacket packet = new RenderPacket(
					entity.getId(), instance.value(),
					skinIndex, animIndex);

			ServerLevel level = (ServerLevel) entity.level();
			double entityX = entity.getX();
			double entityY = entity.getY();
			double entityZ = entity.getZ();

			double maxDistSq = ModConfig.getInstance().maxRenderDistance;
			maxDistSq *= maxDistSq;
			for (ServerPlayer player : level.players()) {
				if (player.distanceToSqr(entityX, entityY, entityZ) > maxDistSq)
					continue;
				player.connection.send(packet);
			}
		});

		animationRegistry.setOnReload(() -> {
			if (this.server == null)
				return;
			var packet = new SyncAnimationDataPacket(animationRegistry.getAll());
			for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
				player.connection.send(packet);
			}
		});

		skinDefinitions.setOnReload(() -> {
			if (this.server == null)
				return;

			var texPacket = collectTexturePacket(skinDefinitions);
			if (texPacket != null) {
				for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
					player.connection.send(texPacket);
				}
			}

			var packet = new SyncSkinDataPacket(skinDefinitions.getAll());
			for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
				player.connection.send(packet);
			}
		});
	}

	private static SyncSpriteTexturePacket collectTexturePacket(SkinDefinitionRegistry skinDefinitions) {
		var rm = skinDefinitions.getResourceManager();
		if (rm == null)
			return null;

		var textures = new LinkedHashMap<Identifier, byte[]>();
		for (var entry : skinDefinitions.getAll().entrySet()) {
			SkinDefinition def = entry.getValue();
			if (def instanceof SpriteSkinDefinition spriteDef) {
				Identifier textureId = spriteDef.texture();
				Identifier pngPath = Identifier.fromNamespaceAndPath(
						textureId.getNamespace(), "textures/" + textureId.getPath() + ".png");
				try {
					var resource = rm.getResourceOrThrow(pngPath);
					try (var in = resource.open()) {
						textures.put(textureId, in.readAllBytes());
					}
				} catch (Exception e) {
					Constants.LOG.warn("Failed to read texture '{}' for skin '{}': {}",
							pngPath, entry.getKey(), e.getMessage());
				}
			}
		}
		return textures.isEmpty() ? null : new SyncSpriteTexturePacket(textures);
	}
}
