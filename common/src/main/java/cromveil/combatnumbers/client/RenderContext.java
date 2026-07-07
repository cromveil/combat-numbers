package cromveil.combatnumbers.client;

import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.render.RenderOption;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.config.IConfigState;

public record RenderContext(IConfigState config, FloatingTextManager textManager) {

	public void tickTexts(double gameTime) {
		if (!config.get(Configs.ENABLED)) {
			textManager.clear();
			return;
		}
		for (FloatingText text : textManager.getActive()) {
			text.setGameTime(gameTime);
		}
		textManager.cleanupExpired();
	}

	public boolean shouldRenderWorld() {
		return config.get(Configs.ENABLED)
				&& !config.get(Configs.RENDER_MODE).isHud();
	}

	public boolean shouldRenderHud() {
		return config.get(Configs.ENABLED)
				&& config.get(Configs.RENDER_MODE).isHud();
	}

	public RenderOption renderOption() {
		return config.get(Configs.RENDER_MODE);
	}
}
