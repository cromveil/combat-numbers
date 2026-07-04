package cromveil.combatnumbers.resource;

import com.mojang.serialization.Codec;
import cromveil.combatnumbers.core.ResourceId;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeReloadRegistry implements ReloadListenerRegistry {

	private final List<Entry> serverEntries = new ArrayList<>();
	private final List<Entry> clientEntries = new ArrayList<>();

	private record Entry(ResourceId name, String directory,
			Codec<?> codec, DataConsumer<?> consumer) {
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
		event.addListener(ResourceIds.to(entry.name()),
				new MinecraftReloadListener(entry.codec(), entry.directory(), entry.consumer()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addListener(AddClientReloadListenersEvent event, Entry entry) {
		event.addListener(ResourceIds.to(entry.name()),
				new MinecraftReloadListener(entry.codec(), entry.directory(), entry.consumer()));
	}

	@Override
	public <T> void registerServerData(ResourceId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer) {
		serverEntries.add(new Entry(name, directory, codec, consumer));
	}

	@Override
	public <T> void registerClientResources(ResourceId name, String directory,
			Codec<T> codec, DataConsumer<T> consumer) {
		clientEntries.add(new Entry(name, directory, codec, consumer));
	}
}
