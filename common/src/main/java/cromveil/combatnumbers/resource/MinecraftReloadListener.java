package cromveil.combatnumbers.resource;

import com.mojang.serialization.Codec;
import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class MinecraftReloadListener<T> extends SimpleJsonResourceReloadListener<T> {

	private final IResourceLoadCallback<T> consumer;

	public MinecraftReloadListener(Codec<T> codec, String directory, IResourceLoadCallback<T> consumer) {
		super(codec, FileToIdConverter.json(directory));
		this.consumer = consumer;
	}

	@Override
	protected void apply(Map<Identifier, T> entries, ResourceManager manager, ProfilerFiller profiler) {
		Map<StableId, T> converted = new LinkedHashMap<>();
		for (var entry : entries.entrySet()) {
			converted.put(StableIdMapper.from(entry.getKey()), entry.getValue());
		}
		consumer.accept(converted, new MinecraftResourceAccessor(manager));
	}
}
