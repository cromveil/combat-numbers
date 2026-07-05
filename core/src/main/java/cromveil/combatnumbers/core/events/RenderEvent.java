package cromveil.combatnumbers.core.events;

import cromveil.combatnumbers.core.StableId;

public record RenderEvent(int entityId, float value, StableId skinId, StableId animationId) {
}
