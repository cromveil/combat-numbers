package cromveil.combatnumbers.core.events;

public final class CombatNumbersEvents {

	public static final Event<CombatCallback> COMBAT = Event.create(CombatCallback.class);

	public static final Event<RenderCallback> RENDER = Event.create(RenderCallback.class);

	@FunctionalInterface
	public interface CombatCallback {
		void onEvent(CombatEvent instance);
	}

	@FunctionalInterface
	public interface RenderCallback {
		void onEvent(RenderEvent instance);
	}

	private CombatNumbersEvents() {
	}
}
