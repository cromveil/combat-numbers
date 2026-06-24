package cromveil.combatnumbers.filters;

import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.styles.WhenCondition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.Map;

public class FilterLoader extends SimpleJsonResourceReloadListener<List<WhenCondition>> {
	private static final FileToIdConverter LISTER = FileToIdConverter.json("filters");

	private final FilterRegistry registry;

	public FilterLoader(FilterRegistry registry) {
		super(WhenCondition.CODEC.listOf(), LISTER);
		this.registry = registry;
	}

	@Override
	protected void apply(Map<Identifier, List<WhenCondition>> entries,
			ResourceManager manager, ProfilerFiller profiler) {
		registry.clear();
		int count = 0;
		for (var entry : entries.entrySet()) {
			for (var condition : entry.getValue()) {
				registry.register(condition);
				count++;
			}
		}
		Constants.LOG.info("Loaded {} filters across {} files", count, entries.size());
	}
}
