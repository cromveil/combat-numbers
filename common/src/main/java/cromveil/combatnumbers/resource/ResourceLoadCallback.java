package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.StableId;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Callback invoked when JSON datapack / resource-pack data is loaded or
 * reloaded. Receives the deserialized map keyed by {@link StableId} together
 * with a {@link ModResourceAccessor} for loading auxiliary resources
 * (e.g. texture PNGs referenced by the JSON).
 */
@FunctionalInterface
public interface ResourceLoadCallback<T> {

	void accept(Map<StableId, T> data, ModResourceAccessor resources);

	static <T> ResourceLoadCallback<T> from(Consumer<Map<StableId, T>> simple) {
		return (data, resources) -> simple.accept(data);
	}
}
