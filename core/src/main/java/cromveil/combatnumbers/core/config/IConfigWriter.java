package cromveil.combatnumbers.core.config;

public interface IConfigWriter {

	<T> void setValue(ConfigDef<T> key, T value);

	void commit();
}
