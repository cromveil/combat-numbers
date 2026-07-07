package cromveil.combatnumbers.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class MinecraftReloadListener<T> extends SimpleJsonResourceReloadListener {

	private final Codec<T> codec;
	private final IResourceLoadCallback<T> consumer;

	public MinecraftReloadListener(Codec<T> codec, String directory, IResourceLoadCallback<T> consumer) {
		super(new Gson(), directory);
		this.codec = codec;
		this.consumer = consumer;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> entries,
			ResourceManager manager, ProfilerFiller profiler) {
		Map<StableId, T> converted = new LinkedHashMap<>();
		for (var entry : entries.entrySet()) {
			codec.parse(JsonOps.INSTANCE, entry.getValue())
					.result()
					.ifPresent(v -> converted.put(StableIdMapper.from(entry.getKey()), v));
		}
		consumer.accept(converted, new MinecraftResourceAccessor(manager));
	}
}
