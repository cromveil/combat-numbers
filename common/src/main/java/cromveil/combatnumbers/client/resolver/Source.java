package cromveil.combatnumbers.client.resolver;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface Source<K, V> {

	/** @return null if not found. */
	@Nullable
	V get(K key);
}
