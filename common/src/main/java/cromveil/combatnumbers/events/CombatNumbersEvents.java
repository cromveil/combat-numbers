package cromveil.combatnumbers.events;

public final class CombatNumbersEvents {

	/**
	 * Fired when a combat event occurs.
	 */
	public static final Event<CombatCallback> COMBAT = Event.create(CombatCallback.class);

	/**
	 * Fired when a combat event is styled and ready to be sent to the client to be rendered.
	 */
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
