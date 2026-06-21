package cromveil.combatnumbers.client.skins;

public record TextSkin(Integer fillColor, int outlineColor, float scale) implements Skin {
	public TextSkin(Integer fillColor, int outlineColor) {
		this(fillColor, outlineColor, 1.0f);
	}

	@Override
	public SkinRenderer createVisual(String text) {
		return new TextSkinRenderer(text, fillColor, outlineColor);
	}

	@Override
	public float getScale() { return scale; }
	
	public static TextSkin createDefault() {
		return new TextSkin(0xFFFFFFFF, 0xFF000000, 1.0f);
	}
}
