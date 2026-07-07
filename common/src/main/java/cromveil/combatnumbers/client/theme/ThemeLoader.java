package cromveil.combatnumbers.client.theme;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.client.skins.TextureByteSource;
import cromveil.combatnumbers.resource.ModResourceAccessor;
import cromveil.combatnumbers.skins.SkinDefinition;

import java.util.Map;
import java.util.Optional;

public final class ThemeLoader {

	public Optional<LoadedTheme> load(String themeId, ModResourceAccessor resources) {
		if (themeId == null || themeId.isBlank()) {
			return Optional.empty();
		}

		String base = "themes/" + themeId;
		Map<StableId, SkinDefinition> skins = resources.loadJsonDirectory(
				base + "/skins", SkinDefinition.CODEC);
		Map<StableId, Timeline> animations = resources.loadJsonDirectory(
				base + "/animations", TimelineCodec.CODEC);

		if (skins.isEmpty() && animations.isEmpty()) {
			Constants.LOG.warn("Theme '{}' has no skins or animations under {}", themeId,
					"assets/" + Constants.MOD_ID + "/" + base);
			return Optional.empty();
		}

		String textureBase = base + "/textures/";
		TextureByteSource textureBytes = logical -> resources.getBytes(
				StableId.of(Constants.MOD_ID,
						textureBase + logical.path() + ".png"));

		Constants.LOG.info("Loaded theme '{}': {} skins, {} animations",
				themeId, skins.size(), animations.size());
		return Optional.of(new LoadedTheme(skins, animations, textureBytes));
	}

	public record LoadedTheme(
			Map<StableId, SkinDefinition> skins,
			Map<StableId, Timeline> animations,
			TextureByteSource textureBytes) {
	}
}
