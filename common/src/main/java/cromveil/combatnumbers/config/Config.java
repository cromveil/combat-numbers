package cromveil.combatnumbers.config;

import java.util.Objects;

public final class Config {

	private static ConfigStore store;

	private Config() {}

	public static void init(ConfigStore store) {
		if (Config.store == null) {
			Config.store = Objects.requireNonNull(store);
		}
	}

	public static <T> T get(ConfigId<T> key) {
		return Objects.requireNonNull(store, "Config not initialized").get(key);
	}

	public static ConfigStore store() {
		return Objects.requireNonNull(store, "Config not initialized");
	}
}
