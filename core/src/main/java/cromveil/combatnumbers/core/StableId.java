package cromveil.combatnumbers.core;

import com.mojang.serialization.Codec;

import java.util.Objects;

public record StableId(String namespace, String path) implements Comparable<StableId> {

	public static final Codec<StableId> CODEC = Codec.STRING.xmap(StableId::parse, StableId::toString);

	public StableId {
		Objects.requireNonNull(namespace, "namespace");
		Objects.requireNonNull(path, "path");
	}

	public static StableId of(String namespace, String path) {
		return new StableId(namespace, path);
	}

	public static StableId parse(String id) {
		int colon = id.indexOf(':');
		if (colon < 0) {
			throw new IllegalArgumentException("Invalid resource id: " + id);
		}
		return new StableId(id.substring(0, colon), id.substring(colon + 1));
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	@Override
	public int compareTo(StableId other) {
		int ns = namespace.compareTo(other.namespace);
		return ns != 0 ? ns : path.compareTo(other.path);
	}
}
