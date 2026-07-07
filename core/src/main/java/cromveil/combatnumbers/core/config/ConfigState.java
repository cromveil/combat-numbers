package cromveil.combatnumbers.core.config;

public interface ConfigState {

	<T> T get(ConfigDef<T> key);

	void onChanged(Runnable listener);

	default <T> void onChanged(ConfigDef<T> key, Runnable listener) {
		onChanged(listener);
	}
}
