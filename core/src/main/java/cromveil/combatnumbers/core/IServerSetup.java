package cromveil.combatnumbers.core;

import java.util.HashSet;
import java.util.Set;

@FunctionalInterface
public interface IServerSetup {

	void register();

	static void registerAll(IServerSetup... setups) {
		Set<IServerSetup> seen = new HashSet<>();
		for (IServerSetup s : setups) {
			if (seen.add(s)) {
				s.register();
			}
		}
	}
}
