package cromveil.combatnumbers.neoforge.impl.resource;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import cromveil.combatnumbers.resource.IReloadListenerRegistry;
import cromveil.combatnumbers.resource.IResourceLoadCallback;
import cromveil.combatnumbers.resource.MinecraftReloadListener;

public class NeoForgeReloadRegistry implements IReloadListenerRegistry {

	private final List<Entry<?>> serverEntries = new ArrayList<>();
	private final List<Entry<?>> clientEntries = new ArrayList<>();

	private record Entry<T>(Codec<T> codec, String directory, IResourceLoadCallback<T> consumer) {
	}

	public NeoForgeReloadRegistry(IEventBus modEventBus) {
		modEventBus.addListener(RegisterClientReloadListenersEvent.class, e -> {
			for (var entry : clientEntries) {
				e.registerReloadListener(createListener(entry));
			}
		});
		NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, e -> {
			for (var entry : serverEntries) {
				e.addListener(createListener(entry));
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static MinecraftReloadListener createListener(Entry<?> entry) {
		return new MinecraftReloadListener(entry.codec(), entry.directory(), entry.consumer());
	}

	@Override
	public <T> void registerServerData(cromveil.combatnumbers.core.StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		serverEntries.add(new Entry<>(codec, directory, consumer));
	}

	@Override
	public <T> void registerClientResources(cromveil.combatnumbers.core.StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		clientEntries.add(new Entry<>(codec, directory, consumer));
	}
}