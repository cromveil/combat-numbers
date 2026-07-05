package cromveil.combatnumbers.client.animation;

import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.resolver.LayeredResolver;
import cromveil.combatnumbers.core.resolver.MapLayer;

import java.util.List;
import java.util.Map;

public final class AnimationResolver {

	private final MapLayer<StableId, Timeline> server = new MapLayer<>();
	private final MapLayer<StableId, Timeline> resourcePack = new MapLayer<>();
	private final MapLayer<StableId, Timeline> theme = new MapLayer<>();
	private final LayeredResolver<StableId, Timeline> resolver = new LayeredResolver<>(
			List.of(server, resourcePack, theme));

	public Timeline resolve(StableId id) {
		Timeline timeline = resolver.resolve(id);
		return timeline != null ? timeline : Timeline.DEFAULT;
	}

	public void setServer(Map<StableId, Timeline> animations) {
		server.set(animations);
	}

	public void setResourcePack(Map<StableId, Timeline> animations) {
		resourcePack.set(animations);
	}

	public void setTheme(Map<StableId, Timeline> animations) {
		theme.set(animations);
	}

	public void clearServer() {
		server.clear();
	}
}
