package cromveil.combatnumbers.resource;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public interface ReloadListenerRegistry {

	<T> void registerServerData(Identifier name, String directory,
			Codec<T> codec, DataConsumer<T> consumer);

	<T> void registerClientResources(Identifier name, String directory,
			Codec<T> codec, DataConsumer<T> consumer);
}
