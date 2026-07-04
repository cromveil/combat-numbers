package cromveil.combatnumbers.core.styles;

import cromveil.combatnumbers.core.ResourceId;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record StyleTable(List<ResourceId> skinIds, List<ResourceId> animationIds) {

	public static final StyleTable EMPTY = new StyleTable(List.of(), List.of());

	public StyleTable {
		skinIds = List.copyOf(skinIds);
		animationIds = List.copyOf(animationIds);
	}

	public static StyleTable from(RuleEngine engine) {
		return new StyleTable(engine.emittableSkinIds(), engine.emittableAnimationIds());
	}

	public int skinIndex(@Nullable ResourceId id) {
		return id == null ? -1 : skinIds.indexOf(id);
	}

	public int animationIndex(@Nullable ResourceId id) {
		return id == null ? -1 : animationIds.indexOf(id);
	}

	@Nullable
	public ResourceId skinAt(int index) {
		return (index < 0 || index >= skinIds.size()) ? null : skinIds.get(index);
	}

	@Nullable
	public ResourceId animationAt(int index) {
		return (index < 0 || index >= animationIds.size()) ? null : animationIds.get(index);
	}
}
