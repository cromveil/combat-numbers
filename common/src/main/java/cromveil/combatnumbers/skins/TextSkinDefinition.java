package cromveil.combatnumbers.skins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record TextSkinDefinition(Integer fillColor, Integer outlineColor, float scale) implements SkinDefinition {
	static final MapCodec<TextSkinDefinition> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			SkinDefinition.COLOR_CODEC.optionalFieldOf("fill_color").forGetter(d -> Optional.ofNullable(d.fillColor)),
			SkinDefinition.COLOR_CODEC.optionalFieldOf("outline_color").forGetter(d -> Optional.ofNullable(d.outlineColor)),
			Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(TextSkinDefinition::scale)
		).apply(instance, (fill, outline, scale) -> new TextSkinDefinition(fill.orElse(null), outline.orElse(null), scale))
	);

	@Override
	public String type() {
		return "text";
	}
}
