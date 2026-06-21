package cromveil.combatnumbers.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class CombatNumbersEvents {

	/**
	 * Fired when a combat event occurs.
	 */
	public static final Event<CombatCallback> COMBAT = EventFactory.createArrayBacked(
			CombatCallback.class,
			instance -> {},
			callbacks -> instance -> {
				for (CombatCallback cb : callbacks) {
					cb.onEvent(instance);
				}
			});

	/**
	 * Fired when a combat event is styled and ready to be sent to the client to be rendered.
	 */
	public static final Event<RenderCallback> RENDER = EventFactory.createArrayBacked(
			RenderCallback.class,
			instance -> {},
			callbacks -> instance -> {
				for (RenderCallback cb : callbacks) {
					cb.onEvent(instance);
				}
			});

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
