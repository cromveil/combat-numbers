package cromveil.combatnumbers.events;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

// Simple event bus
public class Event<T> {

	public static <T> Event<T> create(Class<T> type) {
		return new Event<>(type);
	}

	private final List<T> listeners = new ArrayList<>();
	private final Class<T> type;

	private T invoker;

	private Event(Class<T> type) {
		this.type = type;
	}

	public void register(T listener) {
		listeners.add(listener);
	}

	@SuppressWarnings("unchecked")
	public T invoker() {
		if (invoker == null) {
			invoker = (T) Proxy.newProxyInstance(
					type.getClassLoader(),
					new Class<?>[]{type},
					(proxy, method, args) -> {
						for (T listener : listeners) {
							method.invoke(listener, args);
						}
						return null;
					});
		}
		return invoker;
	}
}
