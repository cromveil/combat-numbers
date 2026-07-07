package cromveil.combatnumbers.fabric.impl.resource;

import com.mojang.serialization.Codec;
import net.minecraft.server.packs.PackType;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.resource.IReloadListenerRegistry;
import cromveil.combatnumbers.resource.IResourceLoadCallback;
import cromveil.combatnumbers.resource.MinecraftReloadListener;

public class FabricReloadRegistry implements IReloadListenerRegistry {

	@Override
	public <T> void registerServerData(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(
				StableIdMapper.to(name), new MinecraftReloadListener<>(codec, directory, consumer));
	}

	@Override
	public <T> void registerClientResources(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
				StableIdMapper.to(name), new MinecraftReloadListener<>(codec, directory, consumer));
	}
}
