package cromveil.combatnumbers.client.animation;

import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.client.resolver.LayeredResolver;
import cromveil.combatnumbers.client.resolver.MapLayer;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public final class AnimationResolver {

	private final MapLayer<Identifier, Timeline> server = new MapLayer<>();
	private final MapLayer<Identifier, Timeline> resourcePack = new MapLayer<>();
	private final MapLayer<Identifier, Timeline> theme = new MapLayer<>();
	private final LayeredResolver<Identifier, Timeline> resolver = new LayeredResolver<>(
			List.of(server, resourcePack, theme));

	public Timeline resolve(Identifier id) {
		Timeline timeline = resolver.resolve(id);
		return timeline != null ? timeline : Timeline.DEFAULT;
	}

	public void setServer(Map<Identifier, Timeline> animations) {
		server.set(animations);
	}

	public void setResourcePack(Map<Identifier, Timeline> animations) {
		resourcePack.set(animations);
	}

	public void setTheme(Map<Identifier, Timeline> animations) {
		theme.set(animations);
	}

	public void clearServer() {
		server.clear();
	}
}
