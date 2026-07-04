package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.ResourceId;
import com.mojang.serialization.Codec;

public interface ReloadListenerRegistry {

	<T> void registerServerData(ResourceId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer);

	<T> void registerClientResources(ResourceId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer);
}
