package cromveil.combatnumbers.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public interface ModResourceAccessor {

	byte @Nullable [] getBytes(StableId location);

	List<StableId> findResources(String directory, Predicate<String> pathPredicate);

	default <T> Map<StableId, T> loadJsonDirectory(String directory, Codec<T> codec) {
		Map<StableId, T> result = new LinkedHashMap<>();
		for (StableId fileId : findResources(directory, path -> path.endsWith(".json"))) {
			byte[] bytes = getBytes(fileId);
			if (bytes == null) {
				continue;
			}
			try {
				String name = fileId.path().substring(fileId.path().lastIndexOf('/') + 1,
						fileId.path().length() - ".json".length());
				StableId id = StableId.of(fileId.namespace(), name);
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
