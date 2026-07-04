package cromveil.combatnumbers.core.styles;

import cromveil.combatnumbers.core.ResourceId;
import org.jspecify.annotations.Nullable;

public record Style(
	@Nullable ResourceId skinId,
	@Nullable ResourceId animationId
) {
	public Style merge(Style override) {
		return new Style(
			override.skinId != null ? override.skinId : this.skinId,
			override.animationId != null ? override.animationId : this.animationId
		);
	}
}
