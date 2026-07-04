package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.core.ResourceId;

import org.jspecify.annotations.Nullable;

/**
 * Supplies raw PNG bytes for a logical texture id, however the owning layer
 * stores them (synced server bytes, client resources, or theme jar files).
 */
@FunctionalInterface
public interface TextureByteSource {

	/** @return the PNG bytes or null. */
	@Nullable
	byte[] get(ResourceId logicalTexture);
}
