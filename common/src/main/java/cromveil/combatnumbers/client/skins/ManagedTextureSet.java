package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.platform.NativeImage;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.StableIdMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Manages DynamicTextures.
 * 
 * NOTE: Must be used on the render thread.
 */
public class ManagedTextureSet {

	private final Set<StableId> registered = new LinkedHashSet<>();

	public boolean has(StableId id) {
		return registered.contains(id);
	}

	public void register(StableId id, byte[] pngData) {
		if (registered.contains(id)) {
			return;
		}
		try {
			NativeImage image = NativeImage.read(new ByteArrayInputStream(pngData));
			DynamicTexture texture = new DynamicTexture(() -> "CombatNumbers: " + id, image);
			Minecraft.getInstance().getTextureManager().register(StableIdMapper.to(id), texture);
			registered.add(id);
		} catch (Exception e) {
			Constants.LOG.warn("Failed to register texture '{}'", id, e);
		}
	}

	public void releaseAll() {
		var textureManager = Minecraft.getInstance().getTextureManager();
		for (StableId id : registered) {
			textureManager.release(StableIdMapper.to(id));
		}
		registered.clear();
	}
}
