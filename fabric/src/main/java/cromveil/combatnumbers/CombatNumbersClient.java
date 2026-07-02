package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.animation.codec.TimelineCodec;
import cromveil.combatnumbers.client.ClientRuntime;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.styles.StyleTable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.Map;

public class CombatNumbersClient implements ClientModInitializer {

	private final ClientRuntime runtime = new ClientRuntime();

	@Override
	public void onInitializeClient() {
		Config.store().addChangeListener(runtime::reloadTheme);

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
				Identifier.fromNamespaceAndPath("combatnumbers", "skins"),
				new SimpleJsonResourceReloadListener<SkinDefinition>(SkinDefinition.CODEC,
						FileToIdConverter.json("skins")) {
					@Override
					protected void apply(Map<Identifier, SkinDefinition> entries, ResourceManager manager,
							ProfilerFiller profiler) {
						runtime.applyResourcePackSkins(entries, manager);
					}
				});
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
				Identifier.fromNamespaceAndPath("combatnumbers", "animations"),
				new SimpleJsonResourceReloadListener<Timeline>(TimelineCodec.CODEC,
						FileToIdConverter.json("animations")) {
					@Override
					protected void apply(Map<Identifier, Timeline> entries, ResourceManager manager,
							ProfilerFiller profiler) {
						runtime.applyResourcePackAnimations(entries);
					}
				});

		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(SyncStyleTablePacket.TYPE, (packet, context) ->
					context.client().execute(() ->
							runtime.applyStyleTable(new StyleTable(packet.skinIds(), packet.animationIds()))));

			ClientPlayNetworking.registerReceiver(SyncAnimationDataPacket.TYPE, (packet, context) ->
					context.client().execute(() -> runtime.applyServerAnimations(packet.animations())));

			ClientPlayNetworking.registerReceiver(SyncSkinDataPacket.TYPE, (packet, context) ->
					context.client().execute(() -> runtime.applyServerSkins(packet.skins())));

			ClientPlayNetworking.registerReceiver(SyncSpriteTexturePacket.TYPE, (packet, context) ->
					context.client().execute(() -> runtime.applyServerTextures(packet.textures())));

			ClientPlayNetworking.registerReceiver(RenderPacket.TYPE, (payload, context) ->
					context.client().execute(() -> runtime.onRenderPacket(
							payload.entityId(), payload.value(),
							payload.skinIndex(), payload.animationIndex())));
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> runtime.onDisconnect());

		WorldRenderEvents.BEFORE_ENTITIES.register(context -> {
			if (!Config.get(ConfigIds.CLIENT_ENABLED)) {
				FloatingTextManager.clear();
				return;
			}

			Minecraft mc = Minecraft.getInstance();
			var level = mc.level;
			if (level == null) {
				FloatingTextManager.clear();
				return;
			}
			double gameTime = level.getGameTime()
					+ mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

			for (FloatingText text : FloatingTextManager.getActive()) {
				text.setGameTime(gameTime);
			}
			FloatingTextManager.cleanupExpired();

			RenderOption option = Config.get(ConfigIds.RENDER_MODE);
			if (option.isHud()) {
				return;
			}

			FloatingTextRenderer.renderAll(BillboardStrategy.create(
					option,
					context.matrices(),
					context.commandQueue(),
					context.worldState().cameraRenderState));
		});
	}
}
