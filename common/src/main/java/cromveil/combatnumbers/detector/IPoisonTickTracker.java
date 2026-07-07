package cromveil.combatnumbers.detector;

public interface IPoisonTickTracker {
	void combatNumbers$setPoisonTick(boolean value);
	boolean combatNumbers$getAndClearPoisonTick();
}
