package cromveil.combatnumbers.core.events;

import cromveil.combatnumbers.core.StableId;

/**
 * Emitted after the style engine has resolved a {@link CombatEvent} into a skin and animation.
 */
public record DispatchEvent(int entityId, float value, StableId skinId, StableId animationId) {
}
