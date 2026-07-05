package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import cromveil.combatnumbers.core.styles.Style;
import cromveil.combatnumbers.resource.StableIdMapper;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record Rule(WhenCondition when, Style then) {
	public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			WhenCondition.CODEC.fieldOf("when").forGetter(Rule::when),
			StyleCodec.CODEC.fieldOf("then").forGetter(Rule::then)
		).apply(instance, Rule::new)
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
