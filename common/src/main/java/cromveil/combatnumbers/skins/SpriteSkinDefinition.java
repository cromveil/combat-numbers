package cromveil.combatnumbers.skins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record SpriteSkinDefinition(
	Identifier texture,
	int columns,
	int cellWidth,
	int cellHeight,
	String charOrder,
	@Nullable Integer fillColor,
	float letterSpacing,
	Map<String, Float> advances,
	boolean colored,
	float scale
) implements SkinDefinition {
	public SpriteSkinDefinition {
		if (cellHeight < 0) cellHeight = cellWidth;
	}

	static final MapCodec<SpriteSkinDefinition> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			Identifier.CODEC.fieldOf("texture").forGetter(SpriteSkinDefinition::texture),
			Codec.INT.fieldOf("columns").forGetter(SpriteSkinDefinition::columns),
			Codec.INT.fieldOf("cell_width").forGetter(SpriteSkinDefinition::cellWidth),
			Codec.INT.optionalFieldOf("cell_height", -1).forGetter(SpriteSkinDefinition::cellHeight),
			Codec.STRING.fieldOf("char_order").forGetter(SpriteSkinDefinition::charOrder),
			SkinDefinition.COLOR_CODEC.optionalFieldOf("fill_color").forGetter(d -> Optional.ofNullable(d.fillColor)),
			Codec.FLOAT.optionalFieldOf("letter_spacing", 0.0f).forGetter(SpriteSkinDefinition::letterSpacing),
			Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("advances", Map.of()).forGetter(SpriteSkinDefinition::advances),
			Codec.BOOL.optionalFieldOf("colored", true).forGetter(SpriteSkinDefinition::colored),
			Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(SpriteSkinDefinition::scale)
		).apply(instance, (texture, columns, cellWidth, cellHeight, charOrder, fillColor, spacing, advances, colored, scale) ->
			new SpriteSkinDefinition(texture, columns, cellWidth, cellHeight, charOrder, fillColor.orElse(null), spacing, advances, colored, scale)
		)
	);

	@Override
	public String type() {
		return "sprite";
	}
}
