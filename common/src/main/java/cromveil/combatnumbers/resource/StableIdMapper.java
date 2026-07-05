package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.StableId;
import net.minecraft.resources.Identifier;

public final class StableIdMapper {

	private StableIdMapper() {
	}

	public static StableId from(Identifier id) {
		return StableId.of(id.getNamespace(), id.getPath());
	}

	public static Identifier to(StableId id) {
		return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
	}
}
