package cromveil.combatnumbers.config;

public interface ConfigStore {

	<T> T get(ConfigId<T> key);

	<T> void set(ConfigId<T> key, T value);

	void save();

	void addChangeListener(Runnable listener);
}
