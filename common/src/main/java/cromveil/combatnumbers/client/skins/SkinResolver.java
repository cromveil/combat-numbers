package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.resolver.LayeredResolver;
import cromveil.combatnumbers.skins.SkinDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkinResolver {

	private static final ISkin DEFAULT = TextSkin.createDefault();

	private final LazySkinLayer server = new LazySkinLayer("server");
	private final LazySkinLayer resourcePack = new LazySkinLayer("rp");
	private final LazySkinLayer theme = new LazySkinLayer("theme");
	private final LayeredResolver<StableId, ISkin> resolver = new LayeredResolver<>(
			List.of(server, resourcePack, theme));

	private Map<StableId, SkinDefinition> serverSkinDefs = Map.of();
	private Map<StableId, byte[]> serverTextureBytes = new LinkedHashMap<>();

	public ISkin resolve(StableId id) {
		ISkin skin = resolver.resolve(id);
		return skin != null ? skin : DEFAULT;
	}

	public void setServerSkinDefs(Map<StableId, SkinDefinition> defs) {
		this.serverSkinDefs = Map.copyOf(defs);
		rebuildServerSkins();
	}

	public void setServerTextureBytes(Map<StableId, byte[]> textures) {
		this.serverTextureBytes = new LinkedHashMap<>(textures);
		rebuildServerSkins();
	}

	public void setResourcePack(Map<StableId, SkinDefinition> defs, ITextureByteSource textures) {
		resourcePack.set(defs, textures);
	}

	public void setTheme(Map<StableId, SkinDefinition> defs, ITextureByteSource textures) {
		theme.set(defs, textures);
	}

	public void clearServer() {
		server.clear();
		serverSkinDefs = Map.of();
		serverTextureBytes.clear();
	}

	private void rebuildServerSkins() {
		server.set(serverSkinDefs, logical -> serverTextureBytes.get(logical));
	}
}
