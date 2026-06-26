package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.client.resolver.LayeredResolver;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public final class SkinResolver {

	private static final Skin DEFAULT = TextSkin.createDefault();

	private final LazySkinLayer server = new LazySkinLayer("server");
	private final LazySkinLayer resourcePack = new LazySkinLayer("rp");
	private final LazySkinLayer theme = new LazySkinLayer("theme");
	private final LayeredResolver<Identifier, Skin> resolver = new LayeredResolver<>(
			List.of(server, resourcePack, theme));

	public Skin resolve(Identifier id) {
		Skin skin = resolver.resolve(id);
		return skin != null ? skin : DEFAULT;
	}

	public void setServer(Map<Identifier, SkinDefinition> defs, TextureByteSource textures) {
		server.set(defs, textures);
	}

	public void setResourcePack(Map<Identifier, SkinDefinition> defs, TextureByteSource textures) {
		resourcePack.set(defs, textures);
	}

	public void setTheme(Map<Identifier, SkinDefinition> defs, TextureByteSource textures) {
		theme.set(defs, textures);
	}

	public void clearServer() {
		server.clear();
	}
}
