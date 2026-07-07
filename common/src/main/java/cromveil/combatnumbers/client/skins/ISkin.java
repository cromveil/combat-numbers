package cromveil.combatnumbers.client.skins;

public interface ISkin {
	ISkinRenderer createVisual(String text);
	default float getScale() { return 1.0f; }
}
