package cromveil.combatnumbers.core;

import java.util.HashSet;
import java.util.Set;

@FunctionalInterface
public interface Setup {

	void register();

	static void registerAll(Setup... setups) {
		Set<Setup> seen = new HashSet<>();
		for (Setup s : setups) {
			if (seen.add(s)) {
				s.register();
			}
		}
	}
}
