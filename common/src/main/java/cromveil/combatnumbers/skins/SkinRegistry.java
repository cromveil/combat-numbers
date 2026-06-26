package cromveil.combatnumbers.skins;

import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.packets.SyncSpriteTexturePacket;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkinRegistry extends SimpleJsonResourceReloadListener<SkinDefinition> {

	private static final FileToIdConverter LISTER = FileToIdConverter.json("skins");

	private final Map<Identifier, SkinDefinition> definitions = new LinkedHashMap<>();
	private ResourceManager resourceManager;
	private Runnable onReload = () -> {
	};

	public SkinRegistry() {
		super(SkinDefinition.CODEC, LISTER);
	}

	@Override
	protected void apply(Map<Identifier, SkinDefinition> entries, ResourceManager manager, ProfilerFiller profiler) {
		this.resourceManager = manager;
		definitions.clear();
		definitions.putAll(entries);
		Constants.LOG.info("Loaded {} skin definitions from server data", definitions.size());
		onReload.run();
	}

	public Map<Identifier, SkinDefinition> getAll() {
		return new LinkedHashMap<>(definitions);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}

	/**
	 * Reads the PNG bytes for every sprite skin so the server can stream them to
	 * clients (whose resource packs won't contain the override textures).
	 *
	 * @return a texture packet, or null if there are no sprite textures to send.
	 */
	public SyncSpriteTexturePacket buildTexturePacket() {
		if (resourceManager == null) {
			return null;
		}
		var textures = new LinkedHashMap<Identifier, byte[]>();
		for (var entry : definitions.entrySet()) {
			if (entry.getValue() instanceof SpriteSkinDefinition sprite) {
				Identifier texture = sprite.texture();
				Identifier png = Identifier.fromNamespaceAndPath(
						texture.getNamespace(), "textures/" + texture.getPath() + ".png");
				try (var in = resourceManager.getResourceOrThrow(png).open()) {
					textures.put(texture, in.readAllBytes());
				} catch (Exception e) {
					Constants.LOG.warn("Failed to read texture '{}' for skin '{}': {}",
							png, entry.getKey(), e.getMessage());
				}
			}
		}
		return textures.isEmpty() ? null : new SyncSpriteTexturePacket(textures);
	}
}
