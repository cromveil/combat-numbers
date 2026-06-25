package cromveil.combatnumbers.client.render;

public enum RenderOption {
	WORLD,
	SCREEN,
	HUD;

	public static RenderOption fromConfig(String mode) {
		if ("hud".equals(mode)) {
			return HUD;
		}
		if ("screen".equals(mode)) {
			return SCREEN;
		}
		return WORLD;
	}

	public boolean isHud() {
		return this == HUD;
	}
}
