package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.core.resolver.Source;
import cromveil.combatnumbers.skins.SkinDefinition;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Source that lazily evaluates skin compilation.
 * 
 * NOTE: Must be used on the render thread.
 */
public final class LazySkinLayer implements Source<ResourceId, Skin> {

	private final String layerPrefix;
	private final ManagedTextureSet textures = new ManagedTextureSet();
	private final Map<ResourceId, Skin> cache = new HashMap<>();
	private Map<ResourceId, SkinDefinition> defs = Map.of();
	private TextureByteSource byteSource = id -> null;

	public LazySkinLayer(String layerPrefix) {
		this.layerPrefix = layerPrefix;
	}

	/** Replaces this layer's skins, releasing the previously compiled textures. */
	public void set(Map<ResourceId, SkinDefinition> defs, TextureByteSource byteSource) {
		textures.releaseAll();
		cache.clear();
		this.defs = Map.copyOf(defs);
		this.byteSource = byteSource;
	}

	public void clear() {
		textures.releaseAll();
		cache.clear();
		defs = Map.of();
		byteSource = id -> null;
	}

	@Override
	@Nullable
	public Skin get(ResourceId id) {
		SkinDefinition def = defs.get(id);
		if (def == null) {
			return null;
		}
		return cache.computeIfAbsent(id, key -> SkinCompiler.compile(def, layerPrefix, textures, byteSource));
	}
}
