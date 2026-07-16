package cromveil.combatnumbers.modules.client;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.client.theme.ThemeDiscoverer;
import cromveil.combatnumbers.client.theme.ThemeLoader;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.resource.IModResourceAccessor;

public final class ThemeModule implements IClientSetup {

	private final IConfigState config;
	private final ThemeLoader themeLoader;
	private final SkinResolver skinResolver;
	private final AnimationResolver animationResolver;
	private final Supplier<IModResourceAccessor> resources;

	public ThemeModule(IConfigState config, ThemeLoader themeLoader,
			SkinResolver skinResolver, AnimationResolver animationResolver,
			Supplier<IModResourceAccessor> resources) {
		this.config = config;
		this.themeLoader = themeLoader;
		this.skinResolver = skinResolver;
		this.animationResolver = animationResolver;
		this.resources = resources;
	}

	@Override
	public void register() {
		config.onChanged(Configs.CLIENT_THEME, this::reload);
	}

	public void reload() {
		var res = resources.get();
		if (res == null) {
			return;
		}
		ThemeDiscoverer.discover(res);
		String themeId = config.get(Configs.CLIENT_THEME);
		Optional<ThemeLoader.LoadedTheme> loaded = themeLoader.load(themeId, res);
		if (loaded.isPresent()) {
			var theme = loaded.get();
			skinResolver.setTheme(theme.skins(), theme.textureBytes());
			animationResolver.setTheme(theme.animations());
		} else {
			skinResolver.setTheme(Map.of(), id -> null);
			animationResolver.setTheme(Map.of());
		}
	}
}
