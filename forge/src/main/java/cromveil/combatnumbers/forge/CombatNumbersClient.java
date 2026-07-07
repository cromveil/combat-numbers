package cromveil.combatnumbers.forge;

import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.client.theme.ThemeLoader;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.ConfigDef.Category;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.config.MergedConfig;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.packets.SyncAnimationDataPacket;
import cromveil.combatnumbers.packets.SyncSkinDataPacket;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.packets.SyncStyleTablePacket;

import cromveil.combatnumbers.forge.impl.config.ConfigFiles;
import cromveil.combatnumbers.forge.impl.platform.ForgeNetwork;
import cromveil.combatnumbers.forge.impl.resource.ForgeReloadRegistry;

import cromveil.combatnumbers.modules.client.ReadResourcePacksModule;
import cromveil.combatnumbers.modules.client.ThemeModule;
import cromveil.combatnumbers.forge.modules.client.FloatingTextRendererModule;
import cromveil.combatnumbers.forge.modules.client.SyncReceiver;

public final class CombatNumbersClient {

	private CombatNumbersClient() {}

	public static void setup(BusGroup modBusGroup, ModContainer container,
			ForgeNetwork network, IConfigState commonConfig) {

		var textManager = new FloatingTextManager();
		var skinResolver = new SkinResolver();
		var animationResolver = new AnimationResolver();

		var serverStyles = new SyncReceiver(animationResolver, skinResolver);

		var clientConfig = ConfigFiles.of(modBusGroup, container, ModConfig.Type.CLIENT, Configs.CLIENT);
		var config = new MergedConfig(Map.of(
				Category.COMMON, commonConfig,
				Category.CLIENT, clientConfig));
		ConfigFiles.registerConfigScreen(container, config, config);

		var factory = new FloatingTextFactory(config, textManager, skinResolver,
				animationResolver, new AnimationCompiler(), serverStyles::styleTable);

		SimpleChannel channel = ChannelBuilder.named(Constants.MOD_ID + ":network")
				.simpleChannel();

		registerPacket(channel, RenderPacket.class,
				(StreamCodec<FriendlyByteBuf, RenderPacket>) (Object) RenderPacket.STREAM_CODEC,
				(packet, ctx) -> factory.onRenderPacket(
						packet.entityId(), packet.value(),
						packet.skinIndex(), packet.animationIndex()));

		registerPacket(channel, SyncAnimationDataPacket.class,
				(StreamCodec<FriendlyByteBuf, SyncAnimationDataPacket>) (Object) SyncAnimationDataPacket.STREAM_CODEC,
				(packet, ctx) -> serverStyles.setAnimations(packet.animations()));

		registerPacket(channel, SyncSkinDataPacket.class,
				(StreamCodec<FriendlyByteBuf, SyncSkinDataPacket>) (Object) SyncSkinDataPacket.STREAM_CODEC,
				(packet, ctx) -> serverStyles.setSkins(packet.skins()));

		registerPacket(channel, SyncSpriteTexturePacket.class,
				(StreamCodec<FriendlyByteBuf, SyncSpriteTexturePacket>) (Object) SyncSpriteTexturePacket.STREAM_CODEC,
				(packet, ctx) -> serverStyles.setTextures(packet.textures()));

		registerPacket(channel, SyncStyleTablePacket.class,
				(StreamCodec<FriendlyByteBuf, SyncStyleTablePacket>) (Object) SyncStyleTablePacket.STREAM_CODEC,
				(packet, ctx) -> serverStyles.setStyleTable(new StyleTable(
						packet.skinIds().stream().map(StableIdMapper::from).toList(),
						packet.animationIds().stream().map(StableIdMapper::from).toList())));

		channel.build();
		network.setChannel(channel);

		var resourcePacks = new ReadResourcePacksModule(skinResolver, animationResolver,
				new ForgeReloadRegistry());
		var theme = new ThemeModule(config, new ThemeLoader(), skinResolver, animationResolver,
				resourcePacks::resources);
		resourcePacks.setOnComplete(theme::reload);

		var renderer = new FloatingTextRendererModule(config, textManager, skinResolver, animationResolver,
				new AnimationCompiler(), serverStyles::styleTable);

		MixinBridge.init(new RenderContext(config, textManager));

		IClientSetup.registerAll(
			resourcePacks, theme, serverStyles,
			renderer
		);
	}

	@SuppressWarnings("unchecked")
	private static <T> void registerPacket(SimpleChannel channel, Class<T> type,
			StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, CustomPayloadEvent.Context> handler) {
		channel.messageBuilder(type)
				.direction(PacketFlow.CLIENTBOUND)
				.codec(codec)
				.consumerMainThread(handler)
				.add();
	}
}
