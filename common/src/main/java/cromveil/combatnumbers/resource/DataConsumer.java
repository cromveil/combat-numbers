package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.StableId;

import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface DataConsumer<T> {

	void accept(Map<StableId, T> data, ModResourceAccessor resources);

	static <T> DataConsumer<T> from(Consumer<Map<StableId, T>> simple) {
		return (data, resources) -> simple.accept(data);
	}
}
