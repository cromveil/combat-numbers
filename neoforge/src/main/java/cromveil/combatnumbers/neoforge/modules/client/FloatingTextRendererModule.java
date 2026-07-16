package cromveil.combatnumbers.neoforge.modules.client;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;

public final class FloatingTextRendererModule implements IClientSetup {

	private final IEventBus modEventBus;
	private final FloatingTextFactory factory;
	public FloatingTextRendererModule(IEventBus modEventBus, IConfigState config,
			FloatingTextManager textManager, SkinResolver skinResolver,
			AnimationResolver animationResolver, AnimationCompiler animationCompiler,
			Supplier<StyleTable> styleTable) {
		this.modEventBus = modEventBus;
		this.factory = new FloatingTextFactory(config, textManager, skinResolver,
				animationResolver, animationCompiler, styleTable);
	}

	@Override
	public void register() {

		modEventBus.addListener(RegisterClientPayloadHandlersEvent.class, e ->
				e.register(RenderPacket.TYPE, (payload, context) -> context.enqueueWork(
						() -> factory.onRenderPacket(
								payload.entityId(), payload.value(),
								payload.skinIndex(), payload.animationIndex()))));
	}
}
