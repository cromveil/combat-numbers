package cromveil.combatnumbers.detector;

public interface PoisonTickTracker {
	void combatNumbers$setPoisonTick(boolean value);
	boolean combatNumbers$getAndClearPoisonTick();
}
