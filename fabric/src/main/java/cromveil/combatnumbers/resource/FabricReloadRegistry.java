package cromveil.combatnumbers.resource;

import com.mojang.serialization.Codec;
import cromveil.combatnumbers.core.StableId;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class FabricReloadRegistry implements ReloadListenerRegistry {

	@Override
	public <T> void registerServerData(StableId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer) {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				StableIdMapper.to(name), new MinecraftReloadListener<>(codec, directory, consumer));
	}

	@Override
	public <T> void registerClientResources(StableId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer) {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				StableIdMapper.to(name), new MinecraftReloadListener<>(codec, directory, consumer));
	}
}
