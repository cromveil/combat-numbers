package cromveil.combatnumbers.core.events;

import cromveil.combatnumbers.core.ResourceId;

public record RenderEvent(int entityId, float value, ResourceId skinId, ResourceId animationId) {
}
