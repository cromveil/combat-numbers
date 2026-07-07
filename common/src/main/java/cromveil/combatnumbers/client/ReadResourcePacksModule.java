package cromveil.combatnumbers.client;

import java.util.Map;

import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.Setup;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.resource.ModResourceAccessor;
import cromveil.combatnumbers.resource.ReloadListenerRegistry;
import cromveil.combatnumbers.skins.SkinDefinition;

public final class ReadResourcePacksModule implements Setup {

	private final SkinResolver skinResolver;
	private final AnimationResolver animationResolver;
	private final ReloadListenerRegistry reloadRegistry;
	private final Runnable onComplete;
	private ModResourceAccessor lastResources;
	private int loadedCount;

	public ReadResourcePacksModule(SkinResolver skinResolver, AnimationResolver animationResolver,
			ReloadListenerRegistry reloadRegistry, Runnable onComplete) {
		this.skinResolver = skinResolver;
		this.animationResolver = animationResolver;
		this.reloadRegistry = reloadRegistry;
		this.onComplete = onComplete;
	}

	@Override
	public void register() {
		reloadRegistry.registerClientResources(
				StableId.of(Constants.MOD_ID, "skins"),
				"skins", SkinDefinition.CODEC,
				this::onSkinsLoaded);

		reloadRegistry.registerClientResources(
				StableId.of(Constants.MOD_ID, "animations"),
				"animations", TimelineCodec.CODEC,
				this::onAnimationsLoaded);
	}

	private void onSkinsLoaded(Map<StableId, SkinDefinition> skins, ModResourceAccessor resources) {
		lastResources = resources;
		skinResolver.setResourcePack(skins, logical -> {
			StableId png = StableId.of(
					logical.namespace(), "textures/" + logical.path() + ".png");
			return resources.getBytes(png);
		});
		afterResourceLoaded();
	}

	private void onAnimationsLoaded(Map<StableId, Timeline> animations, ModResourceAccessor resources) {
		animationResolver.setResourcePack(animations);
		afterResourceLoaded();
	}

	private void afterResourceLoaded() {
		// prevents duplicate fires. Little bit hacky though, since it relies on all reload listeners to fire.
		loadedCount++;
		if (loadedCount >= 2) {
			loadedCount = 0;
			onComplete.run();
		}
	}

	public ModResourceAccessor resources() {
		return lastResources;
	}
}
