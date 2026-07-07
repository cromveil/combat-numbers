package cromveil.combatnumbers.core.resolver;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Multi-layered key-value resolver. Goes through layers in order to find the
 * first value for a given key.
 */
public final class LayeredResolver<K, V> {

	private final List<ISource<K, V>> sources;

	/** @param sources the sources to consult, highest priority first. */
	public LayeredResolver(List<? extends ISource<K, V>> sources) {
		this.sources = List.copyOf(sources);
	}

	@Nullable
	public V resolve(@Nullable K key) {
		if (key == null) {
			return null;
		}
		for (ISource<K, V> source : sources) {
			V value = source.get(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}
