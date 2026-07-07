package cromveil.combatnumbers.core.styles;

import cromveil.combatnumbers.core.StableId;
import org.jspecify.annotations.Nullable;

public record Style(
	@Nullable StableId skinId,
	@Nullable StableId animationId
) {
	public Style merge(Style override) {
		return new Style(
			override.skinId != null ? override.skinId : this.skinId,
			override.animationId != null ? override.animationId : this.animationId
		);
	}
}
