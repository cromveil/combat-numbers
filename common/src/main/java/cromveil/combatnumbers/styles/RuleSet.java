package cromveil.combatnumbers.styles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record RuleSet(
	List<Rule> rules
) {
	public static final Codec<RuleSet> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Rule.CODEC.listOf().fieldOf("rules")
				.forGetter(RuleSet::rules)
		).apply(instance, RuleSet::new)
	);
}
