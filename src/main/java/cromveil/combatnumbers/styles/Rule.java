package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Rule(WhenCondition when, Style then) {
	public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			WhenCondition.CODEC.fieldOf("when").forGetter(Rule::when),
			Style.CODEC.fieldOf("then").forGetter(Rule::then)
		).apply(instance, Rule::new)
	);
}
