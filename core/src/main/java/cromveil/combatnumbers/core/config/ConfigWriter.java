package cromveil.combatnumbers.core.config;

public interface ConfigWriter {

	<T> void setValue(ConfigDef<T> key, T value);

	void commit();
}
