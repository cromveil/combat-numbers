package cromveil.combatnumbers.core.events;

public final class CombatNumbersEvents {

	public static final Event<ICombatCallback> COMBAT = Event.create(ICombatCallback.class);

	public static final Event<IDispatchCallback> DISPATCH = Event.create(IDispatchCallback.class);

	public static final Event<Runnable> DATA_RELOADED = Event.create(Runnable.class);

	@FunctionalInterface
	public interface ICombatCallback {
		void onEvent(CombatEvent instance);
	}

	@FunctionalInterface
	public interface IDispatchCallback {
		void onEvent(DispatchEvent instance);
	}

	private CombatNumbersEvents() {
	}
}
