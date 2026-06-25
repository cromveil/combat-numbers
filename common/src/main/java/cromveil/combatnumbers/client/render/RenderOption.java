package cromveil.combatnumbers.client.render;

public enum RenderOption {
	WORLD,
	SCREEN,
	HUD;

	public boolean isHud() {
		return this == HUD;
	}
}
