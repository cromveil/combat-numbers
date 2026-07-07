package cromveil.combatnumbers.core;

import java.util.HashSet;
import java.util.Set;

@FunctionalInterface
public interface ISetup {

	void register();

	static void registerAll(ISetup... setups) {
		Set<ISetup> seen = new HashSet<>();
		for (ISetup s : setups) {
			if (seen.add(s)) {
				s.register();
			}
		}
	}
}
