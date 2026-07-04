package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.ResourceId;

import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface DataConsumer<T> {

	void accept(Map<ResourceId, T> data, ModResourceAccessor resources);

	static <T> DataConsumer<T> from(Consumer<Map<ResourceId, T>> simple) {
		return (data, resources) -> simple.accept(data);
	}
}
