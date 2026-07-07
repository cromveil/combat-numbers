package cromveil.combatnumbers.core.config;

import java.util.Map;

public final class MergedConfig implements IConfigState, IConfigWriter {

	private final Map<ConfigDef.Category, IConfigState> states;

	public MergedConfig(Map<ConfigDef.Category, IConfigState> states) {
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
			((IConfigWriter) state).setValue(id, value);
		}
	}

	@Override
	public void commit() {
		for (var state : states.values()) {
			((IConfigWriter) state).commit();
		}
	}
}
