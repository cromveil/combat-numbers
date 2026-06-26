package cromveil.combatnumbers.styles;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * The finite, ordered set of skin and animation ids the server's rules can
 * emit. The server builds this from the RuleEngine after a styles syncs it to
 * clients. We then reference entries by index instead of sending full ids,
 * keeping packets lean.
 */
public record StyleTable(List<Identifier> skinIds, List<Identifier> animationIds) {

	public static final StyleTable EMPTY = new StyleTable(List.of(), List.of());

	public StyleTable {
		skinIds = List.copyOf(skinIds);
		animationIds = List.copyOf(animationIds);
	}

	public static StyleTable from(RuleEngine engine) {
		return new StyleTable(engine.emittableSkinIds(), engine.emittableAnimationIds());
	}

	/** @return index of the id, or -1 if absent/null (meaning "no opinion"). */
	public int skinIndex(Identifier id) {
		return id == null ? -1 : skinIds.indexOf(id);
	}

	/** @see #skinIndex(Identifier) */
	public int animationIndex(Identifier id) {
		return id == null ? -1 : animationIds.indexOf(id);
	}

	/** @return the id at the index, or null if out of range. */
	public Identifier skinAt(int index) {
		return (index < 0 || index >= skinIds.size()) ? null : skinIds.get(index);
	}

	/** @see #skinAt(int) */
	public Identifier animationAt(int index) {
		return (index < 0 || index >= animationIds.size()) ? null : animationIds.get(index);
	}
}
