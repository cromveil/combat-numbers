package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.ResourceId;
import net.minecraft.resources.Identifier;

public final class ResourceIds {

	private ResourceIds() {
	}

	public static ResourceId from(Identifier id) {
		return ResourceId.of(id.getNamespace(), id.getPath());
	}

	public static Identifier to(ResourceId id) {
		return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
	}
}
