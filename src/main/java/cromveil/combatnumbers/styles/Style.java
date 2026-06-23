package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import java.util.Optional;

public record Style(
	@Nullable Identifier skinId,
	@Nullable Identifier animationId
) {
	public static final Codec<Style> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Identifier.CODEC.optionalFieldOf("skin").forGetter(s -> Optional.ofNullable(s.skinId)),
			Identifier.CODEC.optionalFieldOf("animation").forGetter(s -> Optional.ofNullable(s.animationId))
		).apply(instance, (skin, animation) ->
			new Style(skin.orElse(null), animation.orElse(null))
		)
	);

	public Style merge(Style override) {
		return new Style(
			override.skinId != null ? override.skinId : this.skinId,
			override.animationId != null ? override.animationId : this.animationId
		);
	}
}
