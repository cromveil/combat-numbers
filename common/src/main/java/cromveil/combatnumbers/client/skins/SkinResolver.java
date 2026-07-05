package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.resolver.LayeredResolver;
import cromveil.combatnumbers.skins.SkinDefinition;

import java.util.List;
import java.util.Map;

public final class SkinResolver {

	private static final Skin DEFAULT = TextSkin.createDefault();

	private final LazySkinLayer server = new LazySkinLayer("server");
	private final LazySkinLayer resourcePack = new LazySkinLayer("rp");
	private final LazySkinLayer theme = new LazySkinLayer("theme");
	private final LayeredResolver<StableId, Skin> resolver = new LayeredResolver<>(
			List.of(server, resourcePack, theme));

	public Skin resolve(StableId id) {
		Skin skin = resolver.resolve(id);
		return skin != null ? skin : DEFAULT;
	}

	public void setServer(Map<StableId, SkinDefinition> defs, TextureByteSource textures) {
		server.set(defs, textures);
	}

	public void setResourcePack(Map<StableId, SkinDefinition> defs, TextureByteSource textures) {
		resourcePack.set(defs, textures);
	}

	public void setTheme(Map<StableId, SkinDefinition> defs, TextureByteSource textures) {
		theme.set(defs, textures);
	}

	public void clearServer() {
		server.clear();
	}
}
