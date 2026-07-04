package cromveil.combatnumbers.core;

import java.util.Objects;

public record ResourceId(String namespace, String path) implements Comparable<ResourceId> {

	public ResourceId {
		Objects.requireNonNull(namespace, "namespace");
		Objects.requireNonNull(path, "path");
	}

	public static ResourceId of(String namespace, String path) {
		return new ResourceId(namespace, path);
	}

	public static ResourceId parse(String id) {
		int colon = id.indexOf(':');
		if (colon < 0) {
			throw new IllegalArgumentException("Invalid resource id: " + id);
		}
		return new ResourceId(id.substring(0, colon), id.substring(colon + 1));
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	@Override
	public int compareTo(ResourceId other) {
		int ns = namespace.compareTo(other.namespace);
		return ns != 0 ? ns : path.compareTo(other.path);
	}
}
