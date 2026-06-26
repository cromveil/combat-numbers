package cromveil.combatnumbers.client.resolver;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Simple source backed by a Map.
 */
public final class MapLayer<K, V> implements Source<K, V> {

	private Map<K, V> values = Map.of();

	public void set(Map<K, V> values) {
		this.values = Map.copyOf(values);
	}

	public void clear() {
		this.values = Map.of();
	}

	@Override
	@Nullable
	public V get(K key) {
		return values.get(key);
	}
}
