package cromveil.combatnumbers;

import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.animation.codec.TimelineCodec;
import cromveil.combatnumbers.client.ClientRuntime;
import cromveil.combatnumbers.config.NeoForgeClientConfig;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.styles.StyleTable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class CombatNumbersClient {

	private final ClientRuntime runtime = new ClientRuntime();

	public CombatNumbersClient(IEventBus modEventBus, ModContainer container) {
		container.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC);
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

		modEventBus.addListener(RegisterClientPayloadHandlersEvent.class, e -> {
			e.register(SyncStyleTablePacket.TYPE, (payload, context) -> context.enqueueWork(() ->
					runtime.applyStyleTable(new StyleTable(payload.skinIds(), payload.animationIds()))));

			e.register(SyncAnimationDataPacket.TYPE, (payload, context) -> context.enqueueWork(() ->
					runtime.applyServerAnimations(payload.animations())));

			e.register(SyncSkinDataPacket.TYPE, (payload, context) -> context.enqueueWork(() ->
					runtime.applyServerSkins(payload.skins())));

			e.register(SyncSpriteTexturePacket.TYPE, (payload, context) -> context.enqueueWork(() ->
					runtime.applyServerTextures(payload.textures())));

			e.register(RenderPacket.TYPE, (payload, context) -> context.enqueueWork(() ->
					runtime.onRenderPacket(
							payload.entityId(), payload.value(),
							payload.skinIndex(), payload.animationIndex())));
		});

		modEventBus.addListener(AddClientReloadListenersEvent.class, e -> {
			e.addListener(
					Identifier.fromNamespaceAndPath("combatnumbers", "skins"),
					new SimpleJsonResourceReloadListener<SkinDefinition>(SkinDefinition.CODEC,
							FileToIdConverter.json("skins")) {
						@Override
						protected void apply(Map<Identifier, SkinDefinition> entries,
								ResourceManager manager, ProfilerFiller profiler) {
							runtime.applyResourcePackSkins(entries, manager);
						}
					});
			e.addListener(
					Identifier.fromNamespaceAndPath("combatnumbers", "animations"),
					new SimpleJsonResourceReloadListener<Timeline>(TimelineCodec.CODEC,
							FileToIdConverter.json("animations")) {
						@Override
						protected void apply(Map<Identifier, Timeline> entries,
								ResourceManager manager, ProfilerFiller profiler) {
							runtime.applyResourcePackAnimations(entries);
						}
					});
		});

		NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> runtime.tickThemeWatch());
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> runtime.onDisconnect());
	}
}
