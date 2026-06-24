package cromveil.combatnumbers.events;

import cromveil.combatnumbers.styles.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public record RenderEvent(
	LivingEntity entity,
	float value,
	Identifier skinId,
	Identifier animationId
) {
	public static RenderEvent from(CombatEvent event, Style style) {
		return new RenderEvent(event.entity(), event.value(),
			style.skinId(), style.animationId());
	}
}
