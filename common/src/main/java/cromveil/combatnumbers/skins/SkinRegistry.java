package cromveil.combatnumbers.skins;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import cromveil.combatnumbers.resource.ModResourceAccessor;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkinRegistry {

	private final Map<Identifier, SkinDefinition> definitions = new LinkedHashMap<>();
	private Map<Identifier, byte[]> textureCache = null;
	private Runnable onReload = () -> {
	};

	public void accept(Map<Identifier, SkinDefinition> entries, ModResourceAccessor resources) {
		definitions.clear();
		definitions.putAll(entries);

		var textures = new LinkedHashMap<Identifier, byte[]>();
		for (var entry : entries.entrySet()) {
			if (entry.getValue() instanceof SpriteSkinDefinition sprite) {
				Identifier texture = sprite.texture();
				Identifier png = Identifier.fromNamespaceAndPath(
						texture.getNamespace(), "textures/" + texture.getPath() + ".png");
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

	public Map<Identifier, SkinDefinition> getAll() {
		return new LinkedHashMap<>(definitions);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}

	public SyncSpriteTexturePacket buildTexturePacket() {
		return textureCache == null ? null : new SyncSpriteTexturePacket(textureCache);
	}
}
