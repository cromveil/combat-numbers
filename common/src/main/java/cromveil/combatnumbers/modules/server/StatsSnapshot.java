package cromveil.combatnumbers.modules.server;

import cromveil.combatnumbers.core.StableId;

import java.util.List;

public record StatsSnapshot(
		List<Entry> damageTypes,
		List<Entry> damageTags,
		List<SourceEntry> sources) {

	public record Entry(StableId key, int count) {}

	public record SourceEntry(String key, int count) {}
}
