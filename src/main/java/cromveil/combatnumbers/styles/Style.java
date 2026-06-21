package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import java.util.Optional;

public record Style(
	@Nullable Identifier skinId,
	@Nullable Identifier animationId,
	@Nullable Float scale
) {
	public static final Codec<Style> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Identifier.CODEC.optionalFieldOf("skin").forGetter(s -> Optional.ofNullable(s.skinId)),
			Identifier.CODEC.optionalFieldOf("animation").forGetter(s -> Optional.ofNullable(s.animationId)),
			Codec.FLOAT.optionalFieldOf("scale").forGetter(s -> Optional.ofNullable(s.scale))
		).apply(instance, (skin, animation, scale) ->
			new Style(skin.orElse(null), animation.orElse(null), scale.orElse(null))
		)
	);

	public Style merge(Style override) {
		return new Style(
			override.skinId != null ? override.skinId : this.skinId,
			override.animationId != null ? override.animationId : this.animationId,
			override.scale != null ? override.scale : this.scale
		);
	}
}
