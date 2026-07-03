package cromveil.combatnumbers.resource;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface DataConsumer<T> {

	void accept(Map<Identifier, T> data, ModResourceAccessor resources);

	static <T> DataConsumer<T> from(Consumer<Map<Identifier, T>> simple) {
		return (data, resources) -> simple.accept(data);
	}
}
