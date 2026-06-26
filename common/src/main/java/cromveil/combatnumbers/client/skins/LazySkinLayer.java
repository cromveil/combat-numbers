package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.client.resolver.Source;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Source that lazily evaluates skin compilation.
 * 
 * NOTE: Must be used on the render thread.
 */
public final class LazySkinLayer implements Source<Identifier, Skin> {

	private final String layerPrefix;
	private final ManagedTextureSet textures = new ManagedTextureSet();
	private final Map<Identifier, Skin> cache = new HashMap<>();
	private Map<Identifier, SkinDefinition> defs = Map.of();
	private TextureByteSource byteSource = id -> null;

	public LazySkinLayer(String layerPrefix) {
		this.layerPrefix = layerPrefix;
	}

	/** Replaces this layer's skins, releasing the previously compiled textures. */
	public void set(Map<Identifier, SkinDefinition> defs, TextureByteSource byteSource) {
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
	public Skin get(Identifier id) {
		SkinDefinition def = defs.get(id);
		if (def == null) {
			return null;
		}
		return cache.computeIfAbsent(id, key -> SkinCompiler.compile(def, layerPrefix, textures, byteSource));
	}
}
