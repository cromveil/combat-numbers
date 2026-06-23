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
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombatNumbers implements ModInitializer {
	public static final String MOD_ID = "combatnumbers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private MinecraftServer server;

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay()
				.register(RenderPacket.TYPE, RenderPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay()
				.register(SyncAnimationDataPacket.TYPE, SyncAnimationDataPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay()
				.register(SyncSkinDataPacket.TYPE, SyncSkinDataPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay()
				.register(SyncSpriteTexturePacket.TYPE, SyncSpriteTexturePacket.STREAM_CODEC);

		ModConfig.getInstance();

		ServerLifecycleEvents.SERVER_STARTED.register(x -> this.server = x);
		ServerLifecycleEvents.SERVER_STOPPING.register(x -> this.server = null);

		var animationRegistry = new AnimationRegistry();
		var skinDefinitions = new SkinDefinitionRegistry();
		var ruleEngine = new RuleEngine();
		var ruleLoader = new RuleLoader(ruleEngine);
		var filterRegistry = new FilterRegistry();
		var filterLoader = new FilterLoader(filterRegistry);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "animations"),
				new SimpleJsonResourceReloadListener<>(TimelineCodec.CODEC,
						FileToIdConverter.json("animations")) {
					@Override
					protected void apply(Map<Identifier, Timeline> entries, ResourceManager manager,
							ProfilerFiller profiler) {
						animationRegistry.reload(entries);
					}
				});
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "styles"),
				ruleLoader);
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "skins"),
				skinDefinitions);
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "filters"),
				filterLoader);

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

			double maxDistSq = ModConfig
					.getInstance().maxRenderDistance;
			maxDistSq *= maxDistSq;
			for (ServerPlayer player : level.players()) {
				if (player.distanceToSqr(entityX, entityY, entityZ) > maxDistSq)
					continue;
				ServerPlayNetworking.send(player, packet);
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, _server) -> {
			var animPacket = new SyncAnimationDataPacket(animationRegistry.getAll());
			ServerPlayNetworking.send(handler.getPlayer(), animPacket);

			var texPacket = collectTexturePacket(skinDefinitions);
			if (texPacket != null) {
				ServerPlayNetworking.send(handler.getPlayer(), texPacket);
			}

			var skinPacket = new SyncSkinDataPacket(skinDefinitions.getAll());
			ServerPlayNetworking.send(handler.getPlayer(), skinPacket);
		});

		animationRegistry.setOnReload(() -> {
			if (this.server == null)
				return;
			var packet = new SyncAnimationDataPacket(animationRegistry.getAll());
			for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
				ServerPlayNetworking.send(player, packet);
			}
		});

		skinDefinitions.setOnReload(() -> {
			if (this.server == null)
				return;

			var texPacket = collectTexturePacket(skinDefinitions);
			if (texPacket != null) {
				for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
					ServerPlayNetworking.send(player, texPacket);
				}
			}

			var packet = new SyncSkinDataPacket(skinDefinitions.getAll());
			for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
				ServerPlayNetworking.send(player, packet);
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
					LOGGER.warn("Failed to read texture '{}' for skin '{}': {}",
							pngPath, entry.getKey(), e.getMessage());
				}
			}
		}
		return textures.isEmpty() ? null : new SyncSpriteTexturePacket(textures);
	}
}
