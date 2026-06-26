package cromveil.combatnumbers.client.skins;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SpriteSheet {

	private final Identifier textureId;
	private final int columns;
	private final int rows;
	private final int cellWidth;
	private final int cellHeight;
	private final Map<Character, Integer> charToIndex;
	private final float[] advances;

	private SpriteSheet(Identifier textureId, int columns, int cellWidth, int cellHeight, String charOrder,
			Map<Character, Float> advanceOverrides) {
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

	public static SpriteSheet create(Identifier renderTextureId, int columns, int cellWidth, int cellHeight,
			String charOrder, Map<Character, Float> advanceOverrides) {
		return new SpriteSheet(renderTextureId, columns, cellWidth, cellHeight, charOrder, advanceOverrides);
	}

	public Identifier textureId() {
		return textureId;
	}

	public int columns() {
		return columns;
	}

	public int rows() {
		return rows;
	}

	public int cellWidth() {
		return cellWidth;
	}

	public int cellHeight() {
		return cellHeight;
	}

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

	public float advance(int index) {
		return advances[index];
	}

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
