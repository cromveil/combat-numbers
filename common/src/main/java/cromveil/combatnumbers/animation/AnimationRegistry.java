package cromveil.combatnumbers.animation;

import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.animation.codec.TimelineCodec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnimationRegistry extends SimpleJsonResourceReloadListener<Timeline> {

	private static final FileToIdConverter LISTER = FileToIdConverter.json("animations");

	private final Map<Identifier, Timeline> animations = new LinkedHashMap<>();
	private Runnable onReload = () -> {
	};

	public AnimationRegistry() {
		super(TimelineCodec.CODEC, LISTER);
	}

	@Override
	protected void apply(Map<Identifier, Timeline> entries, ResourceManager manager, ProfilerFiller profiler) {
		animations.clear();
		animations.putAll(entries);
		Constants.LOG.info("Loaded {} animations from server data", animations.size());
		onReload.run();
	}

	public Map<Identifier, Timeline> getAll() {
		return new LinkedHashMap<>(animations);
	}

	public void setOnReload(Runnable callback) {
		this.onReload = callback;
	}
}
