package cromveil.combatnumbers.animation;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnimationRegistry {

	private final Map<StableId, Timeline> animations = new LinkedHashMap<>();
	private Runnable onReload = () -> {
	};

	public void accept(Map<StableId, Timeline> entries) {
		animations.clear();
		animations.putAll(entries);
		Constants.LOG.info("Loaded {} animations from server data", animations.size());
		onReload.run();
	}

	public Map<StableId, Timeline> getAll() {
		return new LinkedHashMap<>(animations);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}
}
