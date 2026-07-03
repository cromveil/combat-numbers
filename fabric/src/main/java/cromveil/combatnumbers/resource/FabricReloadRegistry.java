package cromveil.combatnumbers.resource;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class FabricReloadRegistry implements ReloadListenerRegistry {

	@Override
	public <T> void registerServerData(Identifier name, String directory,
			Codec<T> codec, DataConsumer<T> consumer) {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				name, new MinecraftReloadListener<>(codec, directory, consumer));
	}

	@Override
	public <T> void registerClientResources(Identifier name, String directory,
			Codec<T> codec, DataConsumer<T> consumer) {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				name, new MinecraftReloadListener<>(codec, directory, consumer));
	}
}
