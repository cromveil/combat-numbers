package cromveil.combatnumbers.client.skins;

public interface Skin {
	SkinRenderer createVisual(String text);
	default float getScale() { return 1.0f; }
}
