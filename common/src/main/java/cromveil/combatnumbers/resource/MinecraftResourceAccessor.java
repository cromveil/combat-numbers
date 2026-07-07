package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

class MinecraftResourceAccessor implements IModResourceAccessor {

	private final ResourceManager manager;

	MinecraftResourceAccessor(ResourceManager manager) {
		this.manager = manager;
	}

	@Override
	public byte @Nullable [] getBytes(StableId location) {
		var id = StableIdMapper.to(location);
		Optional<net.minecraft.server.packs.resources.Resource> resource = manager.getResource(id);
		if (resource.isEmpty()) {
			return null;
		}
		try (var in = resource.get().open()) {
			return in.readAllBytes();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<StableId> findResources(String directory, Predicate<String> pathPredicate) {
		List<StableId> result = new ArrayList<>();
		for (var id : manager.listResources(directory,
				mcId -> pathPredicate.test(mcId.getPath())).keySet()) {
			result.add(StableIdMapper.from(id));
		}
		return result;
	}
}
