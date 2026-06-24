package cromveil.combatnumbers.client.skins;

import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.skins.SkinDefinition;
import cromveil.combatnumbers.skins.SpriteSkinDefinition;
import cromveil.combatnumbers.skins.TextSkinDefinition;

import java.util.HashMap;
import net.minecraft.server.packs.resources.ResourceManager;

public class SkinFactory {
	public static Skin create(SkinDefinition def, ResourceManager resources) {
		return switch (def) {
			case TextSkinDefinition t -> new TextSkin(t.fillColor(), t.outlineColor() != null ? t.outlineColor() : 0xFF000000, t.scale());
			case SpriteSkinDefinition s -> {
				var charAdvances = new HashMap<Character, Float>();
				s.advances().forEach((k, v) -> {
					if (!k.isEmpty()) charAdvances.put(k.charAt(0), v);
				});
				SpriteSheet sheet = SpriteSheet.load(s.texture(), s.columns(), s.cellWidth(), s.cellHeight(), s.charOrder(), charAdvances, resources);
				if (sheet == null) {
					Constants.LOG.warn("Failed to load sprite sheet for skin '{}', falling back to text", s);
					yield new TextSkin(null, 0xFF000000, s.scale());
				}
				yield new SpriteSkin(sheet, s.fillColor(), s.letterSpacing(), s.colored(), s.scale());
			}
		};
	}
}
