package cromveil.combatnumbers.client.render;

import java.util.ArrayList;
import java.util.List;

public class InstanceManager {
	private static final List<Instance> instances = new ArrayList<>();
	private static final int MAX_INSTANCES = 256;

	public static void addInstance(Instance instance) {
		instances.add(instance);
		while (instances.size() > MAX_INSTANCES) {
			instances.removeFirst();
		}
	}

	public static void cleanupExpired(double currentGameTime) {
		instances.removeIf(i -> i.isExpired(currentGameTime));
	}

	public static List<Instance> getActiveInstances() {
		return instances;
	}

	public static void clear() {
		instances.clear();
	}
}
