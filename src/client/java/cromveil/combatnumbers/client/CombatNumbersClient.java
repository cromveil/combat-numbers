package cromveil.combatnumbers.client;

import cromveil.combatnumbers.animations.AnimationDefinition;
import cromveil.combatnumbers.animations.AnimationRegistry;
import cromveil.combatnumbers.client.render.Instance;
import cromveil.combatnumbers.client.render.InstanceManager;
import cromveil.combatnumbers.client.render.InstanceRenderer;
import cromveil.combatnumbers.client.render.ValueFormatter;
import cromveil.combatnumbers.client.skins.Skin;
import cromveil.combatnumbers.client.skins.SkinRegistry;
import cromveil.combatnumbers.client.skins.SpriteSheet;
import cromveil.combatnumbers.client.skins.TextSkin;
import cromveil.combatnumbers.config.ModConfig;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CombatNumbersClient implements ClientModInitializer {
	private static final Skin DEFAULT_SKIN = TextSkin.createDefault();
	private static final AnimationDefinition DEFAULT_ANIMATION = AnimationDefinition.createDefault();

	@Override
	public void onInitializeClient() {
		var animationRegistry = new AnimationRegistry();
		var skinRegistry = new SkinRegistry();

		// Resource pack skins (WIP)
		// ────────────────────────────────────────────────────────────────────
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				Identifier.fromNamespaceAndPath("combatnumbers", "skins"),
				new SimpleJsonResourceReloadListener<SkinDefinition>(SkinDefinition.CODEC,
						FileToIdConverter.json("skins")) {
					@Override
					protected void apply(Map<Identifier, SkinDefinition> entries, ResourceManager manager,
							ProfilerFiller profiler) {
						skinRegistry.reload(entries, manager);
					}
				});

		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			// Sync animation and skin datapacks from server
			// ────────────────────────────────────────────────────────────────────
			ClientPlayNetworking.registerReceiver(SyncAnimationDataPacket.TYPE, (packet, context) -> {
				context.client().execute(() -> {
					animationRegistry.clear();
					packet.animations().forEach(animationRegistry::register);
				});
			});
			ClientPlayNetworking.registerReceiver(SyncSkinDataPacket.TYPE, (packet, context) -> {
				context.client().execute(() -> {
					skinRegistry.reloadFromServer(packet.skins(), context.client().getResourceManager());
				});
			});
			ClientPlayNetworking.registerReceiver(SyncSpriteTexturePacket.TYPE, (packet, context) -> {
				context.client().execute(() -> {
					for (var entry : packet.textures().entrySet()) {
						SpriteSheet.registerServerTexture(entry.getKey(), entry.getValue());
					}
				});
			});

			// Listen for combat number packets from server
			// ────────────────────────────────────────────────────────────────────
			// Index 0 = null/default sentinel; real entries start at 1
			ClientPlayNetworking.registerReceiver(RenderPacket.TYPE,
					(payload, context) -> {
						context.client().execute(() -> {
							if (!ModConfig.getInstance().enabled)
								return;

							Minecraft mc = context.client();
							var level = mc.level;
							if (level == null)
								return;

							var entity = level.getEntity(payload.entityId());
							if (!(entity instanceof LivingEntity livingEntity))
								return;

							var camera = mc.gameRenderer.mainCamera();
							Vec3 camPos = camera.position();

							final float SPREAD = 0.1f;
							Vec3 worldPos = livingEntity.getEyePosition().add(
									(ThreadLocalRandom.current().nextFloat() * 2 - 1) * SPREAD,
									(ThreadLocalRandom.current().nextFloat() * 2 - 1) * SPREAD,
									(ThreadLocalRandom.current().nextFloat() * 2 - 1) * SPREAD);

							var clipCtx = new ClipContext(
									camPos, worldPos,
									ClipContext.Block.COLLIDER,
									ClipContext.Fluid.NONE,
									mc.player);
							var hit = level.clip(clipCtx);
							if (hit.getType() == HitResult.Type.BLOCK)
								return;

							var skin = skinRegistry.getByIndex(payload.skinIndex());
							if (skin == null) {
								skin = DEFAULT_SKIN;
							}

							String formattedValue = ValueFormatter.formatValue(payload.value());

							var visual = skin.createVisual(formattedValue);

							var anim = animationRegistry.getByIndex(payload.animationIndex());
							if (anim == null) {
								anim = DEFAULT_ANIMATION;
							}

							double gameTime = level.getGameTime()
									+ mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

							float styleScale = payload.scale() == 0f ? 1.0f : payload.scale();
							float combinedScale = styleScale * skin.getScale();

							var instance = new Instance(
									worldPos, formattedValue,
									visual, anim, combinedScale, gameTime);
							InstanceManager.addInstance(instance);
						});
					});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			InstanceManager.clear();
			animationRegistry.clear();
			skinRegistry.clear();
			SpriteSheet.clearServerTextures();
		});

		// Client render pipeline for combat numbers
		// ────────────────────────────────────────────────────────────────────
		LevelRenderEvents.END_MAIN.register(context -> {
			if (!ModConfig.getInstance().enabled) {
				InstanceManager.clear();
				return;
			}

			Minecraft mc = Minecraft.getInstance();
			var level = mc.level;
			if (level == null) {
				InstanceManager.clear();
				return;
			}
			double gameTime = level.getGameTime()
					+ mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

			InstanceManager.cleanupExpired(gameTime);

			InstanceRenderer.renderAll(
					context.poseStack(),
					context.submitNodeCollector(),
					context.levelState().cameraRenderState,
					gameTime);
		});
	}
}
