package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.skins.SpriteSkinDefinition;
import cromveil.combatnumbers.skins.TextSkinDefinition;
import net.minecraft.resources.Identifier;

import java.util.HashMap;

public final class SkinCompiler {

	private SkinCompiler() {
	}

	public static Skin compile(SkinDefinition def, String layerPrefix, ManagedTextureSet textures,
			TextureByteSource byteSource) {
		return switch (def) {
			case TextSkinDefinition t -> new TextSkin(
					t.fillColor(),
					t.outlineColor() != null ? t.outlineColor() : 0xFF000000,
					t.scale());
			case SpriteSkinDefinition s -> compileSprite(s, layerPrefix, textures, byteSource);
		};
	}

	private static Skin compileSprite(SpriteSkinDefinition s, String layerPrefix, ManagedTextureSet textures,
			TextureByteSource byteSource) {
		Identifier logical = s.texture();
		Identifier renderId = Identifier.fromNamespaceAndPath(
				Constants.MOD_ID, layerPrefix + "/" + logical.getNamespace() + "/" + logical.getPath());

		if (!textures.has(renderId)) {
			byte[] png = byteSource.get(logical);
			if (png == null) {
				Constants.LOG.warn("Missing texture '{}' for sprite skin (layer '{}'); falling back to text",
						logical, layerPrefix);
				return new TextSkin(s.fillColor(), 0xFF000000, s.scale());
			}
			textures.register(renderId, png);
		}

		var charAdvances = new HashMap<Character, Float>();
		s.advances().forEach((k, v) -> {
			if (!k.isEmpty()) {
				charAdvances.put(k.charAt(0), v);
			}
		});

		SpriteSheet sheet = SpriteSheet.create(
				renderId, s.columns(), s.cellWidth(), s.cellHeight(), s.charOrder(), charAdvances);
		return new SpriteSkin(sheet, s.fillColor(), s.letterSpacing(), s.colored(), s.scale());
	}
}
