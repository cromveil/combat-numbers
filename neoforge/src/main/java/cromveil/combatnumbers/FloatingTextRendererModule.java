package cromveil.combatnumbers;

import java.util.function.Supplier;

import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.Setup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.ConfigState;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public final class FloatingTextRendererModule implements Setup {

	private final IEventBus modEventBus;
	private final FloatingTextFactory factory;
	public FloatingTextRendererModule(IEventBus modEventBus, ConfigState config,
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
