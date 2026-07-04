package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.core.resolver.LayeredResolver;
import cromveil.combatnumbers.skins.SkinDefinition;

import java.util.List;
import java.util.Map;

public final class SkinResolver {

	private static final Skin DEFAULT = TextSkin.createDefault();

	private final LazySkinLayer server = new LazySkinLayer("server");
	private final LazySkinLayer resourcePack = new LazySkinLayer("rp");
	private final LazySkinLayer theme = new LazySkinLayer("theme");
	private final LayeredResolver<ResourceId, Skin> resolver = new LayeredResolver<>(
			List.of(server, resourcePack, theme));

	public Skin resolve(ResourceId id) {
		Skin skin = resolver.resolve(id);
		return skin != null ? skin : DEFAULT;
	}

	public void setServer(Map<ResourceId, SkinDefinition> defs, TextureByteSource textures) {
		server.set(defs, textures);
	}

	public void setResourcePack(Map<ResourceId, SkinDefinition> defs, TextureByteSource textures) {
		resourcePack.set(defs, textures);
	}

	public void setTheme(Map<ResourceId, SkinDefinition> defs, TextureByteSource textures) {
		theme.set(defs, textures);
	}

	public void clearServer() {
		server.clear();
	}
}
