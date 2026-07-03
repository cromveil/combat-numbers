package cromveil.combatnumbers.resource;

import com.mojang.serialization.Codec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

class MinecraftReloadListener<T> extends SimpleJsonResourceReloadListener<T> {

	private final DataConsumer<T> consumer;

	MinecraftReloadListener(Codec<T> codec, String directory, DataConsumer<T> consumer) {
		super(codec, FileToIdConverter.json(directory));
		this.consumer = consumer;
	}

	@Override
	protected void apply(Map<Identifier, T> entries, ResourceManager manager, ProfilerFiller profiler) {
		consumer.accept(entries, new MinecraftResourceAccessor(manager));
	}
}
