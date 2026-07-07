package cromveil.combatnumbers.core.config;

import java.util.Map;

public final class MergedConfig implements ConfigState, ConfigWriter {

	private final Map<ConfigDef.Category, ConfigState> states;

	public MergedConfig(Map<ConfigDef.Category, ConfigState> states) {
		this.states = Map.copyOf(states);
	}

	@Override
	public <T> T get(ConfigDef<T> id) {
		var state = states.get(id.category());
		return state != null ? state.get(id) : id.defaultValue();
	}

	@Override
	public void onChanged(Runnable listener) {
		for (var state : states.values()) {
			state.onChanged(listener);
		}
	}

	@Override
	public <T> void onChanged(ConfigDef<T> key, Runnable listener) {
		var state = states.get(key.category());
		if (state != null) {
			state.onChanged(key, listener);
		}
	}

	@Override
	public <T> void setValue(ConfigDef<T> id, T value) {
		var state = states.get(id.category());
		if (state != null) {
			((ConfigWriter) state).setValue(id, value);
		}
	}

	@Override
	public void commit() {
		for (var state : states.values()) {
			((ConfigWriter) state).commit();
		}
	}
}
