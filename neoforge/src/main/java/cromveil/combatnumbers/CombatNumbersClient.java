package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.animation.registry.AnimationRegistry;
import cromveil.combatnumbers.client.animation.AnimationCompiler;
import cromveil.combatnumbers.client.animation.AnimationEvaluator;
import cromveil.combatnumbers.client.animation.AnimationInstance;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class CombatNumbersClient {
	private static final Skin DEFAULT_SKIN = TextSkin.createDefault();

	private AnimationCompiler animationCompiler;
	private final AnimationRegistry animationRegistry = new AnimationRegistry();
	private final SkinRegistry skinRegistry = new SkinRegistry();

	public CombatNumbersClient(IEventBus modEventBus) {
		this.animationCompiler = new AnimationCompiler();

		modEventBus.addListener(RegisterClientPayloadHandlersEvent.class, e -> {
			e.register(SyncAnimationDataPacket.TYPE,
					(payload, context) -> {
						animationRegistry.clear();
						payload.animations().forEach(animationRegistry::register);
					});

			e.register(SyncSkinDataPacket.TYPE,
					(payload, context) -> {
						skinRegistry.reloadFromServer(payload.skins(),
								Minecraft.getInstance().getResourceManager());
					});

			e.register(SyncSpriteTexturePacket.TYPE,
					(payload, context) -> {
						for (var entry : payload.textures().entrySet()) {
							SpriteSheet.registerServerTexture(entry.getKey(), entry.getValue());
						}
					});

			e.register(RenderPacket.TYPE,
					(payload, context) -> context.enqueueWork(() -> {
						Minecraft mc = Minecraft.getInstance();
						if (!ModConfig.getInstance().enabled)
							return;

						var level = mc.level;
						if (level == null)
							return;

						var entity = level.getEntity(payload.entityId());
						if (!(entity instanceof LivingEntity livingEntity))
							return;

						var camera = mc.gameRenderer.mainCamera();
						Vec3 camPos = camera.position();

						Vec3 worldPos = livingEntity.getEyePosition();

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

						var timeline = animationRegistry.getByIndex(payload.animationIndex());
						if (timeline == null) {
							timeline = Timeline.DEFAULT;
						}

						double gameTime = level.getGameTime()
								+ mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

						long seed = ThreadLocalRandom.current().nextLong();
						AnimationEvaluator eval = animationCompiler.compile(
								timeline, formattedValue.length(), seed);
						AnimationInstance anim = new AnimationInstance(eval);

						var text = new FloatingText(
								worldPos, formattedValue,
								visual, anim, skin.getScale(), gameTime);
						FloatingTextManager.add(text);
					}));
		});
		modEventBus.addListener(AddClientReloadListenersEvent.class, e -> {
			e.addListener(
					Identifier.fromNamespaceAndPath("combatnumbers", "skins"),
					new SimpleJsonResourceReloadListener<SkinDefinition>(SkinDefinition.CODEC,
							FileToIdConverter.json("skins")) {
						@Override
						protected void apply(Map<Identifier, SkinDefinition> entries,
								ResourceManager manager, ProfilerFiller profiler) {
							skinRegistry.reload(entries, manager);
						}
					});
		});
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> {
			FloatingTextManager.clear();
			animationRegistry.clear();
			skinRegistry.clear();
			SpriteSheet.clearServerTextures();
		});
	}
}
