package cromveil.combatnumbers.forge.modules.client;

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
	private final IConfigState config;
	private final FloatingTextManager textManager;

	public FloatingTextRendererModule(IConfigState config, FloatingTextManager textManager,
			SkinResolver skinResolver, AnimationResolver animationResolver,
			AnimationCompiler animationCompiler, Supplier<StyleTable> styleTable) {
		this.factory = new FloatingTextFactory(config, textManager, skinResolver,
				animationResolver, animationCompiler, styleTable);
		this.config = config;
		this.textManager = textManager;
	}

	public FloatingTextFactory factory() {
		return factory;
	}

	public IConfigState config() {
		return config;
	}

	public FloatingTextManager textManager() {
		return textManager;
	}

	@Override
	public void register() {
	}
}
