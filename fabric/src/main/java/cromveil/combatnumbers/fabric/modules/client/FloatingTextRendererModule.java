package cromveil.combatnumbers.fabric.modules.client;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import cromveil.combatnumbers.client.RenderContext;
import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.MixinBridge;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.BillboardStrategy;
import cromveil.combatnumbers.client.render.CameraAdapter;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.render.FloatingTextRenderer;
import cromveil.combatnumbers.client.render.SubmitNodeCollectorAdapter;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;

public final class FloatingTextRendererModule implements IClientSetup {

	private final FloatingTextFactory factory;
	public FloatingTextRendererModule(IConfigState config, FloatingTextManager textManager,
			SkinResolver skinResolver, AnimationResolver animationResolver,
			AnimationCompiler animationCompiler, Supplier<StyleTable> styleTable) {
		this.factory = new FloatingTextFactory(config, textManager, skinResolver,
				animationResolver, animationCompiler, styleTable);
	}

	@Override
	public void register() {

		ClientPlayConnectionEvents.INIT.register((handler, client) ->
				ClientPlayNetworking.registerReceiver(RenderPacket.TYPE,
						(payload, context) -> context.client().execute(
								() -> factory.onRenderPacket(
										payload.entityId(), payload.value(),
										payload.skinIndex(), payload.animationIndex()))));

		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) {
				return;
			}

			RenderContext ctx = MixinBridge.CONTEXT;
			if (ctx == null) {
				return;
			}

			double gameTime = mc.level.getGameTime()
					+ mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			ctx.tickTexts(gameTime);

			if (!ctx.shouldRenderWorld()) {
				return;
			}

			var option = ctx.renderOption();

			FloatingTextRenderer.renderAll(BillboardStrategy.create(
					option,
					context.poseStack(),
					new SubmitNodeCollectorAdapter(context.submitNodeCollector()),
					CameraAdapter.from(context.levelState().cameraRenderState)),
					ctx.config(), ctx.textManager());
		});
	}
}
