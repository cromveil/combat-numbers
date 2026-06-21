package cromveil.combatnumbers.client.skins;

import com.mojang.blaze3d.platform.NativeImage;
import cromveil.combatnumbers.CombatNumbers;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class SpriteSheet {

	private final Identifier textureId;
	private final int columns;
	private final int rows;
	private final int cellWidth;
	private final int cellHeight;
	private final Map<Character, Integer> charToIndex;
	private final float[] advances;

	private static final Set<Identifier> SERVER_TEXTURES = new HashSet<>();

	public static void registerServerTexture(Identifier id, byte[] pngData) {
		try {
			NativeImage image = NativeImage.read(new ByteArrayInputStream(pngData));
			DynamicTexture texture = new DynamicTexture(() -> "SpriteTexture: " + id, image);
			Minecraft.getInstance().getTextureManager().register(id, texture);
			SERVER_TEXTURES.add(id);
		} catch (Exception e) {
			CombatNumbers.LOGGER.warn("Failed to register server texture: {}", id, e);
		}
	}

	public static void clearServerTextures() {
		SERVER_TEXTURES.clear();
	}

	private SpriteSheet(Identifier textureId, int columns, int cellWidth, int cellHeight, String charOrder, Map<Character, Float> advanceOverrides) {
		this.textureId = textureId;
		this.columns = columns;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.rows = Math.ceilDiv(charOrder.length(), columns);
		this.charToIndex = new HashMap<>();
		this.advances = new float[charOrder.length()];
		for (int i = 0; i < charOrder.length(); i++) {
			char c = charOrder.charAt(i);
			charToIndex.put(c, i);
			advances[i] = advanceOverrides.getOrDefault(c, 1.0f);
		}
	}

	public static SpriteSheet load(Identifier key, int columns, int cellWidth, int cellHeight, String charOrder) {
		return load(key, columns, cellWidth, cellHeight, charOrder, Map.of(), Minecraft.getInstance().getResourceManager());
	}

	public static SpriteSheet load(Identifier key, int columns, int cellWidth, int cellHeight, String charOrder, Map<Character, Float> advanceOverrides) {
		return load(key, columns, cellWidth, cellHeight, charOrder, advanceOverrides, Minecraft.getInstance().getResourceManager());
	}

	public static SpriteSheet load(Identifier key, int columns, int cellWidth, int cellHeight, String charOrder, Map<Character, Float> advanceOverrides, ResourceManager resourceManager) {
		if (SERVER_TEXTURES.contains(key)) {
			SpriteSheet sheet = new SpriteSheet(key, columns, cellWidth, cellHeight, charOrder, advanceOverrides);
			CombatNumbers.LOGGER.info("Loaded sprite sheet from server texture: {} ({}x{} cells)", key, cellWidth, cellHeight);
			return sheet;
		}
		Identifier pngPath = Identifier.fromNamespaceAndPath(key.getNamespace(), "textures/" + key.getPath() + ".png");
		try {
			var resource = resourceManager.getResourceOrThrow(pngPath);
			try (InputStream in = resource.open()) {
				NativeImage image = NativeImage.read(in);
				DynamicTexture texture = new DynamicTexture(() -> "SpriteSheet: " + key, image);
				Minecraft.getInstance().getTextureManager().register(key, texture);
			}
			SpriteSheet sheet = new SpriteSheet(key, columns, cellWidth, cellHeight, charOrder, advanceOverrides);
			CombatNumbers.LOGGER.info("Loaded sprite sheet: {} ({}x{} cells)", key, cellWidth, cellHeight);
			return sheet;
		} catch (Exception e) {
			CombatNumbers.LOGGER.warn("Failed to load sprite sheet: {}", pngPath, e);
			return null;
		}
	}

	public Identifier textureId() { return textureId; }
	public int columns() { return columns; }
	public int rows() { return rows; }
	public int cellWidth() { return cellWidth; }
	public int cellHeight() { return cellHeight; }

	public int indexForChar(char c) {
		Integer index = charToIndex.get(c);
		if (index == null) {
			if (Character.isLowerCase(c)) {
				index = charToIndex.get(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c)) {
				index = charToIndex.get(Character.toLowerCase(c));
			}
		}
		return index != null ? index : 0;
	}

	public float advance(int index) { return advances[index]; }

	public float uOffset(int index) {
		return (1.0f - advances[index]) / 2f;
	}

	public float minU(int index) {
		return (float) (index % columns) / columns;
	}

	public float maxU(int index) {
		return (float) (index % columns + 1) / columns;
	}

	public float minV(int index) {
		return (float) (index / columns) / rows;
	}

	public float maxV(int index) {
		return (float) (index / columns + 1) / rows;
	}
}
