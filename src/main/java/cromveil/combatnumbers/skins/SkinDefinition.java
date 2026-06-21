package cromveil.combatnumbers.skins;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;

public sealed interface SkinDefinition permits TextSkinDefinition, SpriteSkinDefinition {
	String type();
	float scale();

	Codec<Integer> COLOR_CODEC = Codec.STRING.xmap(
		str -> (int) Long.parseLong(str.replace("0x", "").replace("0X", "").replace("#", ""), 16),
		val -> "0x" + Integer.toHexString(val)
	);

	Codec<SkinDefinition> CODEC = ExtraCodecs.<String, SkinDefinition>dispatchOptionalValue(
		"type", "settings",
		Codec.STRING,
		SkinDefinition::type,
		SkinDefinition::codecForType
	).codec();

	private static Codec<? extends SkinDefinition> codecForType(String type) {
		@SuppressWarnings("unchecked")
		var codec = (Codec<? extends SkinDefinition>) (Codec<?>) switch (type) {
			case "text" -> TextSkinDefinition.CODEC.codec();
			case "sprite" -> SpriteSkinDefinition.CODEC.codec();
			default -> throw new IllegalArgumentException("Unknown skin type: " + type);
		};
		return codec;
	}
}
