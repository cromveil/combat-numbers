package cromveil.combatnumbers.animation;

import cromveil.combatnumbers.Constants;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnimationRegistry {

	private final Map<Identifier, Timeline> animations = new LinkedHashMap<>();
	private Runnable onReload = () -> {
	};

	public void accept(Map<Identifier, Timeline> entries) {
		animations.clear();
		animations.putAll(entries);
		Constants.LOG.info("Loaded {} animations from server data", animations.size());
		onReload.run();
	}

	public Map<Identifier, Timeline> getAll() {
		return new LinkedHashMap<>(animations);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}
}
