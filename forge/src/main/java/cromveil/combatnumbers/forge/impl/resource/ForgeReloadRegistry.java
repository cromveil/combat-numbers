package cromveil.combatnumbers.forge.impl.resource;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.resource.IReloadListenerRegistry;
import cromveil.combatnumbers.resource.IResourceLoadCallback;
import cromveil.combatnumbers.resource.MinecraftReloadListener;

public class ForgeReloadRegistry implements IReloadListenerRegistry {

	private final List<Entry> serverEntries = new ArrayList<>();
	private final List<Entry> clientEntries = new ArrayList<>();

	private record Entry(StableId name, String directory,
			Codec<?> codec, IResourceLoadCallback<?> consumer) {
	}

	public ForgeReloadRegistry(IEventBus modEventBus) {
		MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent e) -> {
			for (var entry : serverEntries) {
				addServerListener(e, entry);
			}
		});
		modEventBus.addListener((RegisterClientReloadListenersEvent e) -> {
			for (var entry : clientEntries) {
				addClientListener(e, entry);
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addServerListener(AddReloadListenerEvent event, Entry entry) {
		event.addListener(new MinecraftReloadListener(
				entry.codec(), entry.directory(), entry.consumer()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addClientListener(RegisterClientReloadListenersEvent event, Entry entry) {
		event.registerReloadListener(new MinecraftReloadListener(
				entry.codec(), entry.directory(), entry.consumer()));
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
