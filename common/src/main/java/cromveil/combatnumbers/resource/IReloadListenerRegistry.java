package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.StableId;
import com.mojang.serialization.Codec;

/**
 * Platform-agnostic registration point for data-driven reload listeners.
 * <p>
 * Each registered listener watches a JSON directory inside datapacks (server)
 * or resource packs (client). On reload the files are deserialized via the
 * given {@link Codec} and the resulting map is delivered to the
 * {@link IResourceLoadCallback}.
 */
public interface IReloadListenerRegistry {

	<T> void registerServerData(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer);

	<T> void registerClientResources(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer);
}
