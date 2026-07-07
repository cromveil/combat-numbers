package cromveil.combatnumbers.core;

import java.util.HashSet;
import java.util.Set;

@FunctionalInterface
public interface IClientSetup {

	void register();

	static void registerAll(IClientSetup... setups) {
		Set<IClientSetup> seen = new HashSet<>();
		for (IClientSetup s : setups) {
			if (seen.add(s)) {
				s.register();
			}
		}
	}
}
