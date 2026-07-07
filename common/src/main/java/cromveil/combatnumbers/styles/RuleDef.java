package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import cromveil.combatnumbers.core.styles.Style;
import cromveil.combatnumbers.StableIdMapper;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record RuleDef(WhenCondition when, Style then) {
	public static final Codec<RuleDef> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			WhenCondition.CODEC.fieldOf("when").forGetter(RuleDef::when),
			StyleCodec.CODEC.fieldOf("then").forGetter(RuleDef::then)
		).apply(instance, RuleDef::new)
	);

	static final class StyleCodec {
		static final Codec<Style> CODEC = RecordCodecBuilder.<Style>create(instance ->
			instance.group(
				Identifier.CODEC.optionalFieldOf("skin").forGetter(s -> Optional.ofNullable(s.skinId() != null ? StableIdMapper.to(s.skinId()) : null)),
				Identifier.CODEC.optionalFieldOf("animation").forGetter(s -> Optional.ofNullable(s.animationId() != null ? StableIdMapper.to(s.animationId()) : null))
			).apply(instance, (skin, animation) ->
				new Style(
					skin.map(StableIdMapper::from).orElse(null),
					animation.map(StableIdMapper::from).orElse(null)
				)
			)
		);
	}
}
