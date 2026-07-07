package cromveil.combatnumbers.core.config;

import java.util.Map;

public final class MergedConfigWriter implements ConfigWriter {

	private final Map<ConfigDef.Category, ConfigWriter> writers;

	public MergedConfigWriter(Map<ConfigDef.Category, ConfigWriter> writers) {
		this.writers = Map.copyOf(writers);
	}

	@Override
	public <T> void setValue(ConfigDef<T> id, T value) {
		var writer = writers.get(id.category());
		if (writer != null) {
			writer.setValue(id, value);
		}
	}

	@Override
	public void commit() {
		for (var writer : writers.values()) {
			writer.commit();
		}
	}
}
