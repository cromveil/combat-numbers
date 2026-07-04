package cromveil.combatnumbers;

import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.client.ClientRuntime;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.CameraAdapter;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.client.render.SubmitNodeCollectorAdapter;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.resource.DataConsumer;
import cromveil.combatnumbers.resource.FabricReloadRegistry;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.resource.ResourceIds;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class CombatNumbersClient implements ClientModInitializer {

	private final ClientRuntime runtime = new ClientRuntime();

	@Override
	public void onInitializeClient() {
		Config.store().addChangeListener(runtime::reloadTheme);

		var reloadRegistry = new FabricReloadRegistry();
		reloadRegistry.registerClientResources(
				Identifier.fromNamespaceAndPath(Constants.MOD_ID, "skins"),
				"skins", SkinDefinition.CODEC,
				runtime::applyResourcePackSkins);
		reloadRegistry.registerClientResources(
				Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animations"),
				"animations", TimelineCodec.CODEC,
				DataConsumer.from(runtime::applyResourcePackAnimations));

		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(SyncStyleTablePacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> runtime.applyStyleTable(
									new StyleTable(
											packet.skinIds().stream().map(ResourceIds::from).toList(),
											packet.animationIds().stream().map(ResourceIds::from).toList()))));

			ClientPlayNetworking.registerReceiver(SyncAnimationDataPacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> runtime.applyServerAnimations(packet.animations())));

			ClientPlayNetworking.registerReceiver(SyncSkinDataPacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> runtime.applyServerSkins(packet.skins())));

			ClientPlayNetworking.registerReceiver(SyncSpriteTexturePacket.TYPE,
					(packet, context) -> context.client().execute(
							() -> runtime.applyServerTextures(packet.textures())));

			ClientPlayNetworking.registerReceiver(RenderPacket.TYPE,
					(payload, context) -> context.client().execute(
							() -> runtime.onRenderPacket(
									payload.entityId(), payload.value(),
									payload.skinIndex(), payload.animationIndex())));
		});

		ClientPlayConnectionEvents.DISCONNECT.register(
				(handler, client) -> runtime.onDisconnect());

		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			if (!Config.get(ConfigIds.ENABLED)) {
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
					context.poseStack(),
					new SubmitNodeCollectorAdapter(context.submitNodeCollector()),
					CameraAdapter.from(context.levelState().cameraRenderState)));
		});
	}
}
