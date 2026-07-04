package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.codec.TimelineCodec;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.client.ClientRuntime;
import cromveil.combatnumbers.config.CombatNumbersOptions;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.NeoForgeConfig;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.resource.DataConsumer;
import cromveil.combatnumbers.resource.NeoForgeReloadRegistry;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.styles.StyleTable;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class CombatNumbersClient {

	private final ClientRuntime runtime = new ClientRuntime();

	public CombatNumbersClient(IEventBus modEventBus, ModContainer container) {
		NeoForgeConfig config = NeoForgeConfig.instance();
		Config.init(config);

		container.registerConfig(ModConfig.Type.CLIENT, config.clientSpec());
		container.registerExtensionPoint(IConfigScreenFactory.class,
				(container1, screen) -> CombatNumbersOptions.createScreen(screen, Config.store()));

		modEventBus.addListener(RegisterClientPayloadHandlersEvent.class, e -> {
			e.register(SyncStyleTablePacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> runtime.applyStyleTable(
							new StyleTable(payload.skinIds(), payload.animationIds()))));

			e.register(SyncAnimationDataPacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> runtime.applyServerAnimations(payload.animations())));

			e.register(SyncSkinDataPacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> runtime.applyServerSkins(payload.skins())));

			e.register(SyncSpriteTexturePacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> runtime.applyServerTextures(payload.textures())));

			e.register(RenderPacket.TYPE, (payload, context) -> context.enqueueWork(
					() -> runtime.onRenderPacket(
							payload.entityId(), payload.value(),
							payload.skinIndex(), payload.animationIndex())));
		});

		var reloadRegistry = new NeoForgeReloadRegistry(modEventBus);
		reloadRegistry.registerClientResources(
				Identifier.fromNamespaceAndPath(Constants.MOD_ID, "skins"),
				"skins", SkinDefinition.CODEC,
				runtime::applyResourcePackSkins);
		reloadRegistry.registerClientResources(
				Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animations"),
				"animations", TimelineCodec.CODEC,
				DataConsumer.from(runtime::applyResourcePackAnimations));

		modEventBus.addListener(ModConfigEvent.Reloading.class, event -> {
			if (event.getConfig().getSpec() == config.clientSpec()) {
				runtime.reloadTheme();
			}
		});
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class,
				e -> runtime.onDisconnect());
	}
}
