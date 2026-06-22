package cromveil.combatnumbers.animations;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnimationRegistry extends SimpleJsonResourceReloadListener<AnimationDefinition> {
	private static final FileToIdConverter LISTER = FileToIdConverter.json("animations");
	private final Map<Identifier, AnimationDefinition> registry = new LinkedHashMap<>();
	private final List<Identifier> orderedIds = new ArrayList<>();
	private Runnable onReload = () -> {};

	public AnimationRegistry() {
		super(AnimationDefinition.CODEC, LISTER);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}

	public void register(Identifier id, AnimationDefinition anim) {
		if (!registry.containsKey(id)) {
			orderedIds.add(id);
		}
		registry.put(id, anim);
	}

	/** Accepts invalid indices, will just return null. */
	public AnimationDefinition getByIndex(int index) {
		if (index < 0 || index >= orderedIds.size()) return null;
		return registry.get(orderedIds.get(index));
	}

	public int indexOf(Identifier id) {
		return orderedIds.indexOf(id);
	}

	public void clear() {
		registry.clear();
		orderedIds.clear();
	}

	public Map<Identifier, AnimationDefinition> getAll() {
		Map<Identifier, AnimationDefinition> ordered = new LinkedHashMap<>();
		for (Identifier id : orderedIds) {
			ordered.put(id, registry.get(id));
		}
		return ordered;
	}

	public int size() {
		return orderedIds.size();
	}

	@Override
	protected void apply(Map<Identifier, AnimationDefinition> entries, ResourceManager manager, ProfilerFiller profiler) {
		registry.clear();
		orderedIds.clear();
		for (var entry : entries.entrySet()) {
			registry.put(entry.getKey(), entry.getValue());
			orderedIds.add(entry.getKey());
		}
		onReload.run();
	}
}
