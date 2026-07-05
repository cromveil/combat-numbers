package cromveil.combatnumbers.skins;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.resource.ModResourceAccessor;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkinRegistry {

	private final Map<StableId, SkinDefinition> definitions = new LinkedHashMap<>();
	private Map<StableId, byte[]> textureCache = null;
	private Runnable onReload = () -> {
	};

	public void accept(Map<StableId, SkinDefinition> entries, ModResourceAccessor resources) {
		definitions.clear();
		definitions.putAll(entries);

		var textures = new LinkedHashMap<StableId, byte[]>();
		for (var entry : entries.entrySet()) {
			if (entry.getValue() instanceof SpriteSkinDefinition sprite) {
				StableId texture = sprite.texture();
				StableId png = StableId.of(texture.namespace(), "textures/" + texture.path() + ".png");
				byte[] bytes = resources.getBytes(png);
				if (bytes != null) {
					textures.put(texture, bytes);
				} else {
					Constants.LOG.warn("Failed to read texture '{}' for skin '{}'",
							png, entry.getKey());
				}
			}
		}
		this.textureCache = textures.isEmpty() ? null : textures;

		Constants.LOG.info("Loaded {} skin definitions from server data", definitions.size());
		onReload.run();
	}

	public Map<StableId, SkinDefinition> getAll() {
		return new LinkedHashMap<>(definitions);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}

	public SyncSpriteTexturePacket buildTexturePacket() {
		return textureCache == null ? null : new SyncSpriteTexturePacket(textureCache);
	}
}
