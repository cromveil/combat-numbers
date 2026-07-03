package cromveil.combatnumbers.resource;

import net.minecraft.resources.Identifier;
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
	public byte @Nullable [] getBytes(Identifier location) {
		Optional<net.minecraft.server.packs.resources.Resource> resource = manager.getResource(location);
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
	public List<Identifier> findResources(String directory, Predicate<String> pathPredicate) {
		return new ArrayList<>(manager.listResources(directory,
				id -> pathPredicate.test(id.getPath())).keySet());
	}
}
