package cromveil.combatnumbers.core.events;

public final class CombatNumbersEvents {

	public static final Event<CombatCallback> COMBAT = Event.create(CombatCallback.class);

	public static final Event<DispatchCallback> DISPATCH = Event.create(DispatchCallback.class);

	public static final Event<Runnable> DATA_RELOADED = Event.create(Runnable.class);

	@FunctionalInterface
	public interface CombatCallback {
		void onEvent(CombatEvent instance);
	}

	@FunctionalInterface
	public interface DispatchCallback {
		void onEvent(DispatchEvent instance);
	}

	private CombatNumbersEvents() {
	}
}
