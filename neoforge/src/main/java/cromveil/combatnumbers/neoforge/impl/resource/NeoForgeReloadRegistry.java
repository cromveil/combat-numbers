package cromveil.combatnumbers.neoforge.impl.resource;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.resource.IReloadListenerRegistry;
import cromveil.combatnumbers.resource.IResourceLoadCallback;
import cromveil.combatnumbers.resource.MinecraftReloadListener;

public class NeoForgeReloadRegistry implements IReloadListenerRegistry {

	private final List<Entry> serverEntries = new ArrayList<>();
	private final List<Entry> clientEntries = new ArrayList<>();

	private record Entry(StableId name, String directory,
			Codec<?> codec, IResourceLoadCallback<?> consumer) {
	}

	public NeoForgeReloadRegistry(IEventBus modEventBus) {
		modEventBus.addListener(AddClientReloadListenersEvent.class, e -> {
			for (var entry : clientEntries) {
				addListener(e, entry);
			}
		});
		NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, e -> {
			for (var entry : serverEntries) {
				addListener(e, entry);
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addListener(AddServerReloadListenersEvent event, Entry entry) {
		event.addListener(StableIdMapper.to(entry.name()),
				new MinecraftReloadListener(entry.codec(), entry.directory(), entry.consumer()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addListener(AddClientReloadListenersEvent event, Entry entry) {
		event.addListener(StableIdMapper.to(entry.name()),
				new MinecraftReloadListener(entry.codec(), entry.directory(), entry.consumer()));
	}

	@Override
	public <T> void registerServerData(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		serverEntries.add(new Entry(name, directory, codec, consumer));
	}

	@Override
	public <T> void registerClientResources(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		clientEntries.add(new Entry(name, directory, codec, consumer));
	}
}
