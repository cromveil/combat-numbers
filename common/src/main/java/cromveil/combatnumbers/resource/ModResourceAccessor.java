package cromveil.combatnumbers.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import cromveil.combatnumbers.Constants;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public interface ModResourceAccessor {

	byte @Nullable [] getBytes(Identifier location);

	List<Identifier> findResources(String directory, Predicate<String> pathPredicate);

	default <T> Map<Identifier, T> loadJsonDirectory(String directory, Codec<T> codec) {
		Map<Identifier, T> result = new LinkedHashMap<>();
		for (Identifier fileId : findResources(directory, path -> path.endsWith(".json"))) {
			byte[] bytes = getBytes(fileId);
			if (bytes == null) {
				continue;
			}
			try {
				String path = fileId.getPath();
				String name = path.substring(path.lastIndexOf('/') + 1,
						path.length() - ".json".length());
				Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(), name);
				JsonElement json = JsonParser.parseString(
						new String(bytes, StandardCharsets.UTF_8));
				DataResult<T> parsed = codec.parse(JsonOps.INSTANCE, json);
				Optional<T> value = parsed.result();
				if (value.isEmpty()) {
					Constants.LOG.warn("Failed to parse JSON resource {}: {}",
							fileId,
							parsed.error().map(DataResult.Error::message).orElse("unknown error"));
				} else {
					result.put(id, value.get());
				}
			} catch (Exception e) {
				Constants.LOG.warn("Failed to parse JSON resource {}: {}", fileId, e.getMessage());
			}
		}
		return result;
	}
}
