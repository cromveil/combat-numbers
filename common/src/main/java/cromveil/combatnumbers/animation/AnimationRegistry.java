package cromveil.combatnumbers.animation;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.core.animation.Timeline;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnimationRegistry {

	private final Map<ResourceId, Timeline> animations = new LinkedHashMap<>();
	private Runnable onReload = () -> {
	};

	public void accept(Map<ResourceId, Timeline> entries) {
		animations.clear();
		animations.putAll(entries);
		Constants.LOG.info("Loaded {} animations from server data", animations.size());
		onReload.run();
	}

	public Map<ResourceId, Timeline> getAll() {
		return new LinkedHashMap<>(animations);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}
}
