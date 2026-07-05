package cromveil.combatnumbers.resource;

import cromveil.combatnumbers.core.StableId;
import com.mojang.serialization.Codec;

public interface ReloadListenerRegistry {

	<T> void registerServerData(StableId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer);

	<T> void registerClientResources(StableId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer);
}
