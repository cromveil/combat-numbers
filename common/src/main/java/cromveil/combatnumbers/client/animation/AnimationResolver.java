package cromveil.combatnumbers.client.animation;

import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.resolver.LayeredResolver;
import cromveil.combatnumbers.core.resolver.MapLayer;

import java.util.List;
import java.util.Map;

public final class AnimationResolver {

	private final MapLayer<ResourceId, Timeline> server = new MapLayer<>();
	private final MapLayer<ResourceId, Timeline> resourcePack = new MapLayer<>();
	private final MapLayer<ResourceId, Timeline> theme = new MapLayer<>();
	private final LayeredResolver<ResourceId, Timeline> resolver = new LayeredResolver<>(
			List.of(server, resourcePack, theme));

	public Timeline resolve(ResourceId id) {
		Timeline timeline = resolver.resolve(id);
		return timeline != null ? timeline : Timeline.DEFAULT;
	}

	public void setServer(Map<ResourceId, Timeline> animations) {
		server.set(animations);
	}

	public void setResourcePack(Map<ResourceId, Timeline> animations) {
		resourcePack.set(animations);
	}

	public void setTheme(Map<ResourceId, Timeline> animations) {
		theme.set(animations);
	}

	public void clearServer() {
		server.clear();
	}
}
