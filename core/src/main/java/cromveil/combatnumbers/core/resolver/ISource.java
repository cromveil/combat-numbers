package cromveil.combatnumbers.core.resolver;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ISource<K, V> {

	/** @return null if not found. */
	@Nullable
	V get(K key);
}
