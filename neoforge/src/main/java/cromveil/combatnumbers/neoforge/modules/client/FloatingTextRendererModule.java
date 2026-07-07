package cromveil.combatnumbers.neoforge.modules.client;

import java.util.function.Supplier;

import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.styles.StyleTable;

public final class FloatingTextRendererModule implements IClientSetup {

	private final FloatingTextFactory factory;
	public FloatingTextRendererModule(IConfigState config,
			FloatingTextManager textManager, SkinResolver skinResolver,
			AnimationResolver animationResolver, AnimationCompiler animationCompiler,
			Supplier<StyleTable> styleTable) {
		this.factory = new FloatingTextFactory(config, textManager, skinResolver,
				animationResolver, animationCompiler, styleTable);
	}

	@Override
	public void register() {
	}

	public FloatingTextFactory factory() {
		return factory;
	}
}