package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.StableId;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Callback invoked when JSON datapack / resource-pack data is loaded or
 * reloaded. Receives the deserialized map keyed by {@link StableId} together
 * with a {@link IModResourceAccessor} for loading auxiliary resources
 * (e.g. texture PNGs referenced by the JSON).
 */
@FunctionalInterface
public interface IResourceLoadCallback<T> {

	void accept(Map<StableId, T> data, IModResourceAccessor resources);

	static <T> IResourceLoadCallback<T> from(Consumer<Map<StableId, T>> simple) {
		return (data, resources) -> simple.accept(data);
	}
}
