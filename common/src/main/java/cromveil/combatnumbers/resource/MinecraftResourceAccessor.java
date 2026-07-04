package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.ResourceId;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

class MinecraftResourceAccessor implements ModResourceAccessor {

	private final ResourceManager manager;

	MinecraftResourceAccessor(ResourceManager manager) {
		this.manager = manager;
	}

	@Override
	public byte @Nullable [] getBytes(ResourceId location) {
		var id = ResourceIds.to(location);
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
	public List<ResourceId> findResources(String directory, Predicate<String> pathPredicate) {
		List<ResourceId> result = new ArrayList<>();
		for (var id : manager.listResources(directory,
				mcId -> pathPredicate.test(mcId.getPath())).keySet()) {
			result.add(ResourceIds.from(id));
		}
		return result;
	}
}
